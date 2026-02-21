package net.metalbrain.paysmart.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.gson.Gson
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.data.native.RoomNativeBridge
import net.metalbrain.paysmart.domain.model.WalletBalanceModel
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.room.entity.WalletBalanceEntity
import net.metalbrain.paysmart.utils.toHexString
import java.util.Locale

class WalletBalanceRepository @Inject constructor(
    private val dao: WalletBalanceDao,
    private val firestore: FirebaseFirestore,
    private val roomPassphraseRepository: RoomPassphraseRepository
) {
    private companion object {
        const val TAG = "WalletBalanceRepo"
        @Volatile
        private var walletSubcollectionReadable: Boolean = true
    }

    private val gson = Gson()

    private data class WalletBalanceStoredPayload(
        val balancesByCurrency: Map<String, Double> = emptyMap(),
        val rewardsPoints: Double = 0.0,
        val updatedAtMs: Long = System.currentTimeMillis()
    )

    fun observeByUserId(userId: String): Flow<WalletBalanceModel?> {
        return dao.observeByUserId(userId).map { entity ->
            entity?.let { decryptEntity(userId, it) }
        }
    }

    suspend fun syncFromServer(userId: String): Result<WalletBalanceModel?> {
        return runCatching {
            val server = loadServerWallet(userId)
            if (server == null) {
                Log.d(TAG, "No server wallet snapshot for userId=$userId")
                return@runCatching null
            }

            upsert(server)
            server
        }
    }

    suspend fun upsert(model: WalletBalanceModel) {
        val encrypted = encryptModel(model)
        dao.upsert(
            WalletBalanceEntity(
                userId = model.userId,
                jsonData = encrypted,
                updatedAt = model.updatedAtMs
            )
        )
    }

    private suspend fun encryptModel(model: WalletBalanceModel): String {
        val keyHex = getRoomKeyHex()
        val payload = WalletBalanceStoredPayload(
            balancesByCurrency = model.balancesByCurrency,
            rewardsPoints = model.rewardsPoints,
            updatedAtMs = model.updatedAtMs
        )
        val json = gson.toJson(payload)
        return RoomNativeBridge.encryptString(json, keyHex)
    }

    private suspend fun decryptEntity(
        userId: String,
        entity: WalletBalanceEntity
    ): WalletBalanceModel? {
        return try {
            val keyHex = getRoomKeyHex()
            val decrypted = RoomNativeBridge.decryptString(entity.jsonData, keyHex)
            if (decrypted.isBlank()) {
                null
            } else {
                val payload = gson.fromJson(decrypted, WalletBalanceStoredPayload::class.java)
                WalletBalanceModel(
                    userId = userId,
                    balancesByCurrency = payload.balancesByCurrency,
                    rewardsPoints = payload.rewardsPoints,
                    updatedAtMs = payload.updatedAtMs
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decrypt wallet row for userId=$userId", e)
            null
        }
    }

    private suspend fun loadServerWallet(userId: String): WalletBalanceModel? {
        val userDocRef = firestore.collection("users").document(userId)
        // Try root user doc first; this path is typically already allowed by existing rules.
        safeGet(userDocRef)?.let { doc ->
            parseWalletDoc(doc, userId)?.let { return it }
        }

        // If subcollection reads are known to be denied by rules, skip noisy retries.
        if (!walletSubcollectionReadable) {
            return null
        }

        val walletDocRefs = listOf(
            userDocRef.collection("wallet").document("current"),
            userDocRef.collection("wallet").document("balances")
        )
        for (docRef in walletDocRefs) {
            safeGet(docRef)?.let { doc ->
                parseWalletDoc(doc, userId)?.let { return it }
            }
        }
        return null
    }

    private suspend fun safeGet(docRef: DocumentReference): DocumentSnapshot? {
        return try {
            docRef.get().await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                if (docRef.path.contains("/wallet/")) {
                    walletSubcollectionReadable = false
                    Log.w(
                        TAG,
                        "Permission denied for wallet subcollection reads; disabling wallet subcollection fetch and falling back to allowed sources."
                    )
                } else {
                    Log.w(TAG, "Permission denied for document read: ${docRef.path}")
                }
                null
            } else {
                throw e
            }
        }
    }

    private fun parseWalletDoc(
        snapshot: DocumentSnapshot,
        userId: String
    ): WalletBalanceModel? {
        val data = snapshot.data ?: return null

        val balances = parseBalanceMap(
            data["balancesByCurrency"]
                ?: data["balances"]
                ?: data["walletBalances"]
        )
        val rewardsPoints = parseDouble(
            data["rewardsPoints"]
                ?: data["rewardPoints"]
                ?: data["rewardEarnedPoints"]
                ?: data["rewardsEarned"]
                ?: data["rewards"]
                ?: data["points"]
        ) ?: 0.0

        if (balances.isEmpty() && rewardsPoints == 0.0) {
            return null
        }

        val updatedAtMs = parseUpdatedAtMillis(data["updatedAt"]) ?: System.currentTimeMillis()

        return WalletBalanceModel(
            userId = userId,
            balancesByCurrency = balances,
            rewardsPoints = rewardsPoints,
            updatedAtMs = updatedAtMs
        )
    }

    private fun parseBalanceMap(raw: Any?): Map<String, Double> {
        val source = raw as? Map<*, *> ?: return emptyMap()
        return source.entries
            .mapNotNull { (k, v) ->
                val key = (k as? String)?.trim()?.uppercase(Locale.US)
                val value = parseDouble(v)
                if (key.isNullOrBlank() || value == null) {
                    null
                } else {
                    key to value
                }
            }
            .toMap()
    }

    private fun parseDouble(raw: Any?): Double? {
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.trim().toDoubleOrNull()
            else -> null
        }
    }

    private fun parseUpdatedAtMillis(raw: Any?): Long? {
        return when (raw) {
            is com.google.firebase.Timestamp -> raw.toDate().time
            is Number -> raw.toLong()
            else -> null
        }
    }

    private suspend fun getRoomKeyHex(): String {
        val keyBytes = roomPassphraseRepository.getRoomKey()
        require(keyBytes.size == 16 || keyBytes.size == 32) {
            "Bad AES key size=${keyBytes.size}"
        }
        return keyBytes.toHexString()
    }
}

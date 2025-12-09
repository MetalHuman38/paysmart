package net.metalbrain.paysmart.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.utils.normalizeProvider

class FirestoreUserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserProfileRepository {

    private val users = firestore.collection("users")

    override fun watchByUid(uid: String): Flow<AuthUserModel?> = callbackFlow {
        val listener = users.document(uid).addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data
            trySend(data?.let { AuthUserModel.fromMap(it) }).isSuccess
        }
        awaitClose { listener.remove() }
    }

    override suspend fun upsertNewUser(user: AuthUserModel, providerId: String) {
        val docRef = users.document(user.uid)

        val normalizedProvider = normalizeProvider(providerId)

        val data = buildMap {
            put("authProvider", normalizedProvider)
            put("email", user.email)
            put("emailVerified", user.emailVerified)
            put("isAnonymous", user.isAnonymous)
            put("status", user.status.name.lowercase()) // Optional: lowercase for consistent Firestore queries
            put("providerIds", user.providerIds)
            put("createdAt", FieldValue.serverTimestamp())
            put("lastSignedIn", FieldValue.serverTimestamp())
            put("hasVerifiedEmail", user.hasVerifiedEmail)
            put("hasAddedHomeAddress", user.hasAddedHomeAddress)
            put("hasVerifiedIdentity", user.hasVerifiedIdentity)
            put("hasLocalPassword", user.hasLocalPassword)
            put("localPasswordSetAt", user.localPasswordSetAt)


            user.displayName?.takeIf { it.isNotBlank() }?.let { put("displayName", it) }
            user.photoURL?.takeIf { it.startsWith("http") }?.let { put("photoURL", it) }
            user.phoneNumber?.takeIf { it.isNotBlank() }?.let { put("phoneNumber", it) }
            user.tenantId?.takeIf { it.isNotBlank() }?.let { put("tenantId", it) }
        }

        docRef.set(data, SetOptions.merge()).await()
    }


    override suspend fun updateProgressFlags(uid: String, progressFlags: Map<String, Boolean>) {
        if (progressFlags.isEmpty()) return
        users.document(uid).update(progressFlags).await()
    }

    private suspend fun updateProgressFlagsInternal(
        uid: String,
        hasVerifiedEmail: Boolean? = null,
        hasAddedHomeAddress: Boolean? = null,
        hasVerifiedIdentity: Boolean? = null
    ) {
        val updates = mutableMapOf<String, Any>()

        hasVerifiedEmail?.let { updates["hasVerifiedEmail"] = it }
        hasAddedHomeAddress?.let { updates["hasAddedHomeAddress"] = it }
        hasVerifiedIdentity?.let { updates["hasVerifiedIdentity"] = it }

        if (updates.isNotEmpty()) {
            users.document(uid).update(updates).await()
        }
    }

    private fun setLocalPasswordFlag(value: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)

        val update = mutableMapOf<String, Any>("hasLocalPassword" to value)
        if (value) update["localPasswordSetAt"] = Timestamp.now()

        userDoc.set(update, SetOptions.merge())
    }


    override suspend fun touchLastSignedIn(uid: String) {
        users.document(uid).update("lastSignedIn", FieldValue.serverTimestamp()).await()
    }
}

package net.metalbrain.paysmart.domain.room
import android.util.Log
import com.google.gson.Gson
import jakarta.inject.Inject
import net.metalbrain.paysmart.data.native.RoomNativeBridge
import net.metalbrain.paysmart.data.repository.RoomPassphraseRepository
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.entity.SecuritySettingsEntity


class NativeRoomUseCase @Inject constructor(
    private val dao: SecuritySettingsDao,
    private val encryptor: NativeRoomEncryptor,
    private val roomPassphraseRepository: RoomPassphraseRepository
) : RoomUseCase {

    private val gson = Gson()



    override fun encrypt(data: String, key: String): String {
        return RoomNativeBridge.encryptString(data, key)
    }

    override fun decrypt(encrypted: String, key: String): String {
        return RoomNativeBridge.decryptString(encrypted, key)
    }

    override suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel) {
        val json = gson.toJson(model)

        val keyBytes = roomPassphraseRepository.getRoomKey()
        require(keyBytes.size == 16 || keyBytes.size == 32) { "Bad AES key size=${keyBytes.size}" }

        val keyHex = keyBytes.toHexString() // ✅ HEX expected by native

        val encrypted = RoomNativeBridge.encryptString(json, keyHex)

        dao.upsert(SecuritySettingsEntity(userId, encrypted, salt = "")) // salt currently unused in AES-GCM
    }

//    override suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel) {
//        val json = gson.toJson(model)
//        val salt = encryptor.generateSaltBase64()
//
//        val keyBytes = roomPassphraseRepository.getRoomKey()
//        val keyB64 = encryptor.encode(keyBytes)
//
//
//        val encrypted = RoomNativeBridge.encryptString(json, keyB64)
//
//        Log.d("RoomUseCase", "🔐 Saving encrypted settings for user=$userId")
//        Log.d("RoomUseCase", "  → JSON=$json")
//        Log.d("RoomUseCase", "  → Encrypted=$encrypted")
//
//        dao.upsert(SecuritySettingsEntity(userId, encrypted, salt))
//    }

//    override suspend fun getSecuritySettings(userId: String): SecuritySettingsModel? {
//        val entity = dao.getByUserId(userId) ?: return null
//
//        val keyBytes = roomPassphraseRepository.getRoomKey()
//        val keyB64 = encryptor.encode(keyBytes)
//
//        val decrypted = RoomNativeBridge.decryptString(entity.jsonData, keyB64)
//        return gson.fromJson(decrypted, SecuritySettingsModel::class.java)
//    }

    override suspend fun getSecuritySettings(userId: String): SecuritySettingsModel? {
        val entity = dao.getByUserId(userId) ?: return null

        val keyBytes = roomPassphraseRepository.getRoomKey()
        require(keyBytes.size == 16 || keyBytes.size == 32) { "Bad AES key size=${keyBytes.size}" }

        val keyHex = keyBytes.toHexString() // ✅

        val decrypted = RoomNativeBridge.decryptString(entity.jsonData, keyHex)
        if (decrypted.isBlank()) return null

        return gson.fromJson(decrypted, SecuritySettingsModel::class.java)
    }

    override suspend fun isReady(): Boolean {
        return try {
            val key = roomPassphraseRepository.getRoomKey()
            key.isNotEmpty()
        } catch (e: Exception) {
            Log.e("RoomUseCase", "Key not ready", e)
            false
        }
    }
}

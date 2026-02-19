package net.metalbrain.paysmart.data.repository

import jakarta.inject.Inject
import net.metalbrain.paysmart.room.converters.EncryptedStringConverter
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.entity.SecureTokenEntity
import java.util.UUID

class SecureTokenRepository @Inject constructor(
    private val dao: SecureTokenDao,
    private val converter: EncryptedStringConverter
) {
    suspend fun saveToken(userId: String, plainToken: String) {
        val encrypted = converter.encrypt(plainToken)
        dao.insert(SecureTokenEntity(UUID.randomUUID().toString(), userId, encrypted))
    }

    suspend fun getTokens(userId: String): List<String> =
        dao.getTokensByUser(userId).map { converter.decrypt(it.salt) }
}

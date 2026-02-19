package net.metalbrain.paysmart.domain.room

import net.metalbrain.paysmart.domain.model.SecuritySettingsModel

interface RoomUseCase {
    fun encrypt(data: String, key: String): String
    fun decrypt(encrypted: String, key: String): String

    suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel)

    suspend fun getSecuritySettings(userId: String): SecuritySettingsModel?

    suspend fun isReady(): Boolean

}

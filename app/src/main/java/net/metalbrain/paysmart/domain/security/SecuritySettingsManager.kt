package net.metalbrain.paysmart.domain.security
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel

interface SecuritySettingsManager {
    suspend fun initialIzeLocalSettings(): Boolean
    suspend fun getCloudSettings(): SecuritySettingsModel?

    suspend fun getLocalSettings(): LocalSecuritySettingsModel?
    suspend fun isLocked(): Boolean
    suspend fun unlockSession()
    suspend fun shouldPromptBiometric(): Boolean

    suspend fun shouldPromptPasscode(): Boolean

    suspend fun isBiometricCompleted(): Boolean

    suspend fun verifyPasscode(passcode: String): Boolean
    suspend fun hasPasscode(): Boolean

    suspend fun hasPassword(): Boolean

    suspend fun clearPasscode()
}

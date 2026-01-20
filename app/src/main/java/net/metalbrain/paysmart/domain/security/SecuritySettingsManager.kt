package net.metalbrain.paysmart.domain.security
import net.metalbrain.paysmart.domain.model.SecuritySettings

interface SecuritySettingsManager {
    suspend fun getCloudSettings(): SecuritySettings?
    suspend fun isLocked(): Boolean
    suspend fun unlockSession()
    suspend fun verifyPasscode(passcode: String): Boolean
    suspend fun hasPasscode(): Boolean
    suspend fun clearPasscode()
}

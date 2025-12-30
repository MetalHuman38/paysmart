package net.metalbrain.paysmart.domain.security
import net.metalbrain.paysmart.domain.model.SecuritySettings

interface SecuritySettingsManager {
    suspend fun getCloudSettings(uid: String): SecuritySettings?
    fun isLocked(): Boolean
    fun unlockSession()
    fun verifyPasscode(passcode: String): Boolean
    fun hasPasscode(): Boolean
    fun clearPasscode()
}

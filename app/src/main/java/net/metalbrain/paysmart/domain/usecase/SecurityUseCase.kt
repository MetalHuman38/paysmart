package net.metalbrain.paysmart.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel

interface SecurityUseCase {

    val settingsFlow: StateFlow<SecuritySettingsModel?>

    val localSettingsFlow: StateFlow<LocalSecuritySettingsModel?>

    suspend fun syncSecuritySettings(userId: String, idToken: String): Result<Unit>
    suspend fun saveSecuritySettings(userId: String, model: SecuritySettingsModel): Result<Unit>

    suspend fun fetchLocalSettings(userId: String, force: Boolean): Result<Unit>

    suspend fun fetchCloudSettings(userId: String): Result<Unit>

    suspend fun markPasscodeEnabledOnServer(): Result<Unit>

    suspend fun getPasscodeEnabledOnServer(): Boolean

    /* ---- suspend (DataStore / Flow) ---- */
    suspend fun isLocked(): Boolean
    suspend fun unlockSession()

    /* ---- synchronous (local only) ---- */
    suspend fun verifyPasscode(passcode: String): Boolean

    suspend fun hasPasscode(): Boolean

    suspend fun hasPassword(): Boolean
    
    suspend fun shouldPromptBiometric(): Boolean

    suspend fun isBiometricCompleted(): Boolean

    suspend fun shouldPromptPasscode(): Boolean

    suspend fun clearPasscode()
}

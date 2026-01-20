package net.metalbrain.paysmart.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import net.metalbrain.paysmart.domain.model.SecuritySettings

interface SecurityUseCase {

    val settingsFlow: StateFlow<SecuritySettings?>

    suspend fun fetchCloudSettings(): Result<Unit>

    suspend fun markPasscodeEnabledOnServer(): Result<Unit>

    suspend fun getPasscodeEnabledOnServer(): Boolean

    /* ---- suspend (DataStore / Flow) ---- */
    suspend fun isLocked(): Boolean
    suspend fun unlockSession()

    /* ---- synchronous (local only) ---- */
    suspend fun verifyPasscode(passcode: String): Boolean
    suspend fun hasPasscode(): Boolean
    suspend fun clearPasscode()
}

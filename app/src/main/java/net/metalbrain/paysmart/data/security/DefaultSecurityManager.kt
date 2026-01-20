package net.metalbrain.paysmart.data.security

import kotlinx.coroutines.flow.first
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import javax.inject.Inject

class DefaultSecurityManager @Inject constructor(
    private val passcodeRepository: PasscodeRepository,
    private val securityPrefs: SecurityPreference
) : SecuritySettingsManager {

    override suspend fun getCloudSettings(): SecuritySettings? {
        return securityPrefs.cloudSecuritySettingsFlow.first()
    }

    override suspend fun isLocked(): Boolean {
        val lastUnlock = securityPrefs.lastUnlockFlow.first()
        val lockAfterMinutes = securityPrefs.lockAfterMinutesFlow.first()
        val now = System.currentTimeMillis()
        val timeoutMillis = lockAfterMinutes * 60 * 1000

        return (now - lastUnlock) > timeoutMillis &&
                passcodeRepository.hasPasscode()
    }

    override suspend fun unlockSession() {
        securityPrefs.updateLastUnlock()
    }


    override suspend fun verifyPasscode(passcode: String): Boolean =
        passcodeRepository.verify(passcode)

    override suspend fun hasPasscode(): Boolean =
        passcodeRepository.hasPasscode()

    override suspend fun clearPasscode() {
        passcodeRepository.clear()
    }
}

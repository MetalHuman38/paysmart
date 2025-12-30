package net.metalbrain.paysmart.data.security

import net.metalbrain.paysmart.core.security.SecurityPrefs
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.SecurityCloudRepository
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import javax.inject.Inject

class DefaultSecurityManager @Inject constructor(
    private val passcodeRepository: PasscodeRepository,
    private val cloudRepository: SecurityCloudRepository
) : SecuritySettingsManager {

    override suspend fun getCloudSettings(uid: String): SecuritySettings? {
        return cloudRepository.getSettings(uid)
    }

    override fun isLocked(): Boolean {
        val lastUnlock = SecurityPrefs.lastUnlockTimestamp
        val now = System.currentTimeMillis()
        val timeoutMillis = SecurityPrefs.lockAfterMinutes * 60 * 1000

        return (now - lastUnlock) > timeoutMillis && passcodeRepository.hasPasscode()
    }

    override fun unlockSession() {
        SecurityPrefs.lastUnlockTimestamp = System.currentTimeMillis()
    }

    override fun verifyPasscode(passcode: String): Boolean {
        return passcodeRepository.verify(passcode)
    }

    override fun hasPasscode(): Boolean {
        return passcodeRepository.hasPasscode()
    }

    override fun clearPasscode() {
        passcodeRepository.clear()
    }
}

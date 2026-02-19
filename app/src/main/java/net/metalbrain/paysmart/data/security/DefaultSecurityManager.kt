package net.metalbrain.paysmart.data.security

import kotlinx.coroutines.flow.first
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.data.repository.BiometricRepository
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.SecurePasswordRepository
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import javax.inject.Inject

class DefaultSecurityManager @Inject constructor(
    private val passcodeRepository: PasscodeRepository,
    private val biometricRepository: BiometricRepository,
    private val securePasswordRepository: SecurePasswordRepository,
    private val securityPrefs: SecurityPreference
) : SecuritySettingsManager {

    override suspend fun initialIzeLocalSettings(): Boolean {
         return securityPrefs.isInitializedFlow.first()
    }


    override suspend fun getCloudSettings(): SecuritySettingsModel? {
        return securityPrefs.cloudSecuritySettingsFlow.first()
    }

    override suspend fun getLocalSettings(): LocalSecuritySettingsModel {
         return securityPrefs.localSecurityStateFlow.first()
    }

    override suspend fun isLocked(): Boolean {
        val local = securityPrefs.loadLocalSecurityState()
        if (local.sessionLocked) {
            return true
        }
        val hasPasscodeUnlock = local.passcodeEnabled && passcodeRepository.hasPasscode()
        val hasPasswordUnlock = local.passwordEnabled && securePasswordRepository.hasPassword()
        val hasBiometricUnlock = local.biometricsEnabled && biometricRepository.isBiometricEnabled()
        val hasAnyUnlockMethod = hasPasscodeUnlock || hasPasswordUnlock || hasBiometricUnlock
        if (!hasAnyUnlockMethod) {
            return false
        }

        val lockAfterMinutes = local.lockAfterMinutes ?: 5
        if (lockAfterMinutes <= 0) {
            return false
        }

        val lastUnlock = securityPrefs.lastUnlockFlow.first()
        if (lastUnlock <= 0L) {
            securityPrefs.updateLastUnlock()
            return false
        }

        val now = System.currentTimeMillis()
        val timeoutMillis = lockAfterMinutes * 60 * 1000

        return (now - lastUnlock) > timeoutMillis
    }

    override suspend fun unlockSession() {
        securityPrefs.unlockSession()
    }

    override suspend fun shouldPromptBiometric(): Boolean {
        val isEnabled = biometricRepository.isBiometricEnabled()
        val isLocked = isLocked()
        return isEnabled && isLocked
    }

    override suspend fun shouldPromptPasscode(): Boolean {
        val isEnabled = passcodeRepository.isPasscodeEnabled()
        val isLocked = isLocked()
        return isEnabled && isLocked
    }

    override suspend fun isBiometricCompleted(): Boolean {
        return biometricRepository.isBiometricSetupComplete()
    }

    override suspend fun verifyPasscode(passcode: String): Boolean =
        passcodeRepository.verify(passcode)

    override suspend fun hasPasscode(): Boolean =
        passcodeRepository.hasPasscode()

    override suspend fun hasPassword(): Boolean =
        securePasswordRepository.hasPassword()

    override suspend fun clearPasscode() {
        passcodeRepository.clear()
    }
}

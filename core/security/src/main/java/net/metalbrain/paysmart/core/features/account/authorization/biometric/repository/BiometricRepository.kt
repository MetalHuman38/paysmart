package net.metalbrain.paysmart.core.features.account.authorization.biometric.repository

import jakarta.inject.Inject
import jakarta.inject.Singleton
import com.google.firebase.Timestamp
import net.metalbrain.paysmart.core.features.account.authorization.biometric.remote.BiometricPolicyHandler
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference

@Singleton
class BiometricRepository @Inject constructor(
    private val securityPreference: SecurityPreference,
    private val biometricPolicyHandler: BiometricPolicyHandler
) {

    suspend fun enableBiometric(idToken: String): Boolean {
        enableBiometricLocal()
        return biometricPolicyHandler.setBiometricEnabled(idToken)
    }

    suspend fun enableBiometricLocal() {
        val updated = securityPreference
            .loadLocalSecurityState()
            .copy(
                biometricsEnabled = true,
                biometricsEnabledAt = Timestamp.now()
            )
        securityPreference.saveLocalSecurityState(updated)
    }

    suspend fun syncBiometricEnabled(idToken: String): Boolean {
        return biometricPolicyHandler.setBiometricEnabled(idToken)
    }

    suspend fun isBiometricEnabled(): Boolean {
        return securityPreference.loadLocalSecurityState().biometricsEnabled
    }

    suspend fun isBiometricSetupComplete(): Boolean {
        val localState = securityPreference.loadLocalSecurityState()
        return localState.biometricsEnabled
    }

    suspend fun clearBiometric() {
        val current = securityPreference.loadLocalSecurityState()
        securityPreference.saveLocalSecurityState(
            current.copy(biometricsEnabled = false, biometricsEnabledAt = null)
        )
    }
}

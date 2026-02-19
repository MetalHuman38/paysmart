package net.metalbrain.paysmart.data.repository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.metalbrain.paysmart.core.auth.BiometricPolicyHandler
import net.metalbrain.paysmart.core.security.SecurityPreference

@Singleton
class BiometricRepository @Inject constructor(
    private val securityPreference: SecurityPreference,
    private val biometricPolicyHandler: BiometricPolicyHandler
) {

    suspend fun enableBiometric(idToken: String): Boolean {
        val serverAccepted = biometricPolicyHandler.setBiometricEnabled(idToken)
        val updated = securityPreference
            .loadLocalSecurityState()
            .copy(biometricsEnabled = serverAccepted)
        securityPreference.saveLocalSecurityState(updated)
        return serverAccepted
    }

    suspend fun isBiometricEnabled(): Boolean {
        return securityPreference.loadLocalSecurityState().biometricsEnabled
    }

    suspend fun isBiometricSetupComplete(): Boolean {
        val localState = securityPreference.loadLocalSecurityState()
        return localState.biometricsRequired && localState.biometricsEnabled
    }

    suspend fun clearBiometric() {
        val current = securityPreference.loadLocalSecurityState()
        securityPreference.saveLocalSecurityState(
            current.copy(biometricsEnabled = false, biometricsEnabledAt = null)
        )
    }
}

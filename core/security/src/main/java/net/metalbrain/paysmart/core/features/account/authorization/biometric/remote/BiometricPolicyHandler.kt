package net.metalbrain.paysmart.core.features.account.authorization.biometric.remote

import android.util.Log
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class BiometricPolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig,
    runtimeConfig: RuntimeConfig
) {
    private val client = BiometricPolicyClient(
        config = config,
        debugLoggingEnabled = runtimeConfig.isDebug
    )

    suspend fun setBiometricEnabled(idToken: String): Boolean {
        return try {
            client.markBiometricEnabled(idToken)
        } catch (e: Exception) {
            Log.e("BiometricPolicyHandler", "Failed to call setBiometricEnabled", e)
            false
        }
    }
}

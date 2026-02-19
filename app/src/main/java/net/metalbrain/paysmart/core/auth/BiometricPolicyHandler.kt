package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class BiometricPolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = BiometricPolicyClient(config)

    suspend fun setBiometricEnabled(idToken: String): Boolean {
        return try {
            client.markBiometricEnabled(idToken)
        } catch (e: Exception) {
            Log.e("BiometricPolicyHandler", "Failed to call setBiometricEnabled", e)
            false
        }
    }
}

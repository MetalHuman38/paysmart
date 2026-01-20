package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class PassCodePolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = PassCodePolicyClient(config)

    suspend fun setPassCodeEnabled(idToken: String): Boolean {
        return try {
            client.markPassCodeEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PasscodePolicyHandler", "Failed to call setPassCodeEnabled", e)
            false
        }
    }

    suspend fun getPasswordEnabled(idToken: String): Boolean {
        return try {
            client.isPassCodeEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PassCodePolicyHandler", "Failed to call isPasscodeEnabled", e)
            false
        }
    }

}

// AuthPolicyHandler.kt
package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class AuthPolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.authBase,
        attachApiPrefix = false
    )

    private val client = AuthPolicyClient(config)

    suspend fun isPhoneAlreadyRegistered(e164: String): Boolean {
        return try {
            client.checkIfPhoneNumberAlreadyExist(e164)
        } catch (e: Exception) {
            Log.e("AuthPolicyHandler", "Phone check failed", e)
            false // fail open
        }
    }
}

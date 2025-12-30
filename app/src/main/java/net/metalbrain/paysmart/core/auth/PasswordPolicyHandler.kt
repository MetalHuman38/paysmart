package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class PasswordPolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = PasswordPolicyClient(config)

    suspend fun setPasswordEnabled(idToken: String): Boolean {
        return try {
            client.markPasswordEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PasswordPolicyHandler", "Failed to call setPasswordEnabled", e)
            false
        }
    }

    suspend fun getPasswordEnabled(idToken: String): Boolean {
        return try {
            client.isPasswordEnabled(idToken)
        } catch (e: Exception) {
            Log.e("PasswordPolicyHandler", "Failed to call isPasswordEnabled", e)
            false
        }
    }

}

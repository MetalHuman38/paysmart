package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class PhoneChangePolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = PhoneChangePolicyClient(config)

    suspend fun confirmPhoneChanged(idToken: String, phoneNumber: String): Boolean {
        return try {
            client.confirmPhoneChanged(idToken, phoneNumber)
        } catch (e: Exception) {
            Log.e("PhoneChangePolicyHandler", "Failed to confirm phone change", e)
            false
        }
    }
}

package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class HomeAddressPolicyHandler @Inject constructor() {
    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = HomeAddressPolicyClient(config)

    suspend fun setHomeAddressVerified(idToken: String): Boolean {
        return try {
            client.markHomeAddressVerified(idToken)
        } catch (e: Exception) {
            Log.e("HomeAddressPolicy", "Failed to call setHomeAddressVerified", e)
            false
        }
    }
}

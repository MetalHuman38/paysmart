package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class PhoneChangePolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {
    private val client = PhoneChangePolicyClient(config)

    suspend fun confirmPhoneChanged(idToken: String, phoneNumber: String): Boolean {
        return try {
            client.confirmPhoneChanged(idToken, phoneNumber)
        } catch (e: Exception) {
            Log.e("PhoneChangePolicyHandler", "Failed to confirm phone change", e)
            throw IllegalStateException(
                e.message ?: "Unable to confirm phone number change. Please retry.",
                e
            )
        }
    }
}

package net.metalbrain.paysmart.core.features.account.address.remote

import android.util.Log
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class HomeAddressPolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {
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

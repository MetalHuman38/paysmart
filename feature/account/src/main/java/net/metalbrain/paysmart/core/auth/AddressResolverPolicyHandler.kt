package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import javax.inject.Inject

class AddressResolverPolicyHandler @Inject constructor(
    @ApiPrefixedAuthConfig config: AuthApiConfig
) {
    private val client = AddressResolverPolicyClient(config)

    suspend fun resolveAddress(
        idToken: String,
        payload: AddressLookupPayload
    ): Result<AddressLookupResult> {
        return try {
            client.lookupAddress(idToken, payload)
        } catch (e: Exception) {
            Log.e("AddressResolverPolicy", "Address lookup failed", e)
            Result.failure(e)
        }
    }
}

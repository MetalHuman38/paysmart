package net.metalbrain.paysmart.core.auth

import android.util.Log
import net.metalbrain.paysmart.Env
import javax.inject.Inject

class AddressResolverPolicyHandler @Inject constructor() {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

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

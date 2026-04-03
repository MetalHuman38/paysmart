package net.metalbrain.paysmart.core.features.account.address.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Client responsible for interacting with the authentication API to manage home address policies.
 *
 * This client provides functionality to update the verification status of a user's home address
 * via remote network calls.
 *
 * @property config The configuration object containing API endpoint URLs.
 * @property httpClient The [OkHttpClient] used to perform network requests. Defaults to a client with a 5-second timeout.
 */
class HomeAddressPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    suspend fun markHomeAddressVerified(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.setHomeAddressVerifiedUrl)
            .header("Authorization", "Bearer $idToken")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }
}

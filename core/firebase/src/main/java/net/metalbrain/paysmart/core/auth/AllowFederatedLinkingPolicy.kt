package net.metalbrain.paysmart.core.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AllowFederatedLinkingPolicy(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {

    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    /**
     * Explicitly allow federated account linking ONCE.
     * Server will auto-clear after successful link.
     */
    suspend fun enableAllowFederatedLinking(idToken: String): Boolean =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(config.allowFederatedLinkingUrl)
                .header("Authorization", "Bearer $idToken")
                .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            Log.d("AllowFederatedLinking", "POST ${request.url}")

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(
                        "AllowFederatedLinking",
                        "Failed: ${response.code} ${response.message}"
                    )
                }
                response.isSuccessful
            }
        }
}

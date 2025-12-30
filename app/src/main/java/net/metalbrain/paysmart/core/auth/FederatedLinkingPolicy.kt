package net.metalbrain.paysmart.core.auth


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class FederatedLinkingPolicy(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkIfFederatedAccountExists(email: String?, phone: String?): List<String> {
        if (email == null && phone == null) return emptyList()

        val json = JSONObject().apply {
            email?.let { put("email", it) }
            phone?.let { put("phoneNumber", it) }
        }

        val request = Request.Builder()
            .url(config.checkPhoneOrEmailUrl)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val jsonResp = JSONObject(body)
            val arr = jsonResp.optJSONArray("existingProviders") ?: return@withContext emptyList()

            List(arr.length()) { arr.getString(it) }
        }
    }
}

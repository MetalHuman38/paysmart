package net.metalbrain.paysmart.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PhoneChangePolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    suspend fun confirmPhoneChanged(
        idToken: String,
        phoneNumber: String
    ): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("phoneNumber", phoneNumber)
            .toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(config.confirmPhoneChangedUrl)
            .header("Authorization", "Bearer $idToken")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }
}

// AuthPolicyClient.kt
package net.metalbrain.paysmart.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {

    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkIfPhoneNumberAlreadyExist(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val mediaType = "application/json".toMediaTypeOrNull()
        val json = JSONObject().put("phoneNumber", phoneNumber).toString()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(config.checkIfPhoneAlreadyExistUrl)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        response.use {
            val responseBody = it.body?.string()
            if (it.isSuccessful) {
                // Phone is available
                return@withContext false
            } else if (responseBody?.contains("already-exists") == true) {
                // Phone is taken
                return@withContext true
            }

            throw Exception("Unexpected response: $responseBody")
        }
    }
}

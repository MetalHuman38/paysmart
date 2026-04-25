// AuthPolicyClient.kt
package net.metalbrain.paysmart.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachRequiredAppCheckToken
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachOptionalAppCheckToken

class AuthPolicyClient(
    private val config: AuthApiConfig,
    private val requireAppCheckToken: Boolean = false,
    private val appCheckTokenProvider: AppCheckTokenProvider? = null,
    private val httpClient: OkHttpClient = defaultClient
) {

    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun checkIfPhoneNumberAlreadyExist(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val mediaType = "application/json".toMediaTypeOrNull()
        val json = JSONObject().put("phoneNumber", phoneNumber).toString()
        val body = json.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(config.checkIfPhoneAlreadyExistUrl)
            .post(body)

        if (requireAppCheckToken) {
            requestBuilder.attachRequiredAppCheckToken(
                appCheckTokenProvider = appCheckTokenProvider,
                endpointName = "/auth/check-phone"
            )
        } else {
            requestBuilder.attachOptionalAppCheckToken(appCheckTokenProvider)
        }

        val request = requestBuilder.build()

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

    suspend fun finalizePhoneSignup(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val body = "{}".toRequestBody("application/json".toMediaTypeOrNull())
        val requestBuilder = Request.Builder()
            .url(config.finalizePhoneSignupUrl)
            .header("Authorization", "Bearer $idToken")
            .post(body)

        if (requireAppCheckToken) {
            requestBuilder.attachRequiredAppCheckToken(
                appCheckTokenProvider = appCheckTokenProvider,
                endpointName = "/auth/finalize-phone-signup"
            )
        } else {
            requestBuilder.attachOptionalAppCheckToken(appCheckTokenProvider)
        }

        httpClient.newCall(requestBuilder.build()).execute().use { response ->
            if (response.isSuccessful) {
                return@withContext true
            }

            val responseBody = response.body?.string()
            val message = responseBody?.let(::extractErrorMessage)
            throw Exception(message ?: "Unable to finish account setup. Please retry.")
        }
    }

    private fun extractErrorMessage(responseBody: String): String? {
        return runCatching {
            val json = JSONObject(responseBody)
            when (val error = json.opt("error")) {
                is JSONObject -> error.optString("message").takeIf { it.isNotBlank() }
                is String -> error.takeIf { it.isNotBlank() }
                else -> null
            }
        }.getOrNull()
    }
}

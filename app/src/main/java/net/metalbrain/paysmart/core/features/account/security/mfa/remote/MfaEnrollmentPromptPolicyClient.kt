package net.metalbrain.paysmart.core.features.account.security.mfa.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MfaEnrollmentPromptPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    suspend fun setPromptState(
        idToken: String,
        hasSkippedMfaEnrollmentPrompt: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("hasSkippedMfaEnrollmentPrompt", hasSkippedMfaEnrollmentPrompt)
            .toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(config.setMfaEnrollmentPromptStateUrl)
            .header("Authorization", "Bearer $idToken")
            .post(payload)
            .build()

        httpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }
}

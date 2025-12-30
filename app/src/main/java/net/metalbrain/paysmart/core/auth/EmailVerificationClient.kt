package net.metalbrain.paysmart.core.auth


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EmailVerificationClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val JSON = "application/json".toMediaType()
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Generate email verification link for user.
     */

    suspend fun generateVerification(idToken: String, email: String): Boolean =
        withContext(Dispatchers.IO) {

            val body = JSONObject()
                .put("email", email.trim().lowercase())
                .toString()
                .toRequestBody(JSON)

            val request = Request.Builder()
                .url(config.generateEmailVerificationHandlerUrl)
                .header("Authorization", "Bearer $idToken")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { resp ->
                resp.isSuccessful
            }
        }

    /**
     * Check Email verification status for user.
     */
    suspend fun checkVerificationStatus(idToken: String, email: String): Boolean =
        withContext(Dispatchers.IO) {

            val body = JSONObject()
                .put("email", email.trim().lowercase())
                .toString()
                .toRequestBody(JSON)

            val request = Request.Builder()
                .url(config.checkEmailVerificationStatusHandlerUrl)
                .header("Authorization", "Bearer $idToken")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext false

                val json = JSONObject(resp.body?.string() ?: "{}")
                json.optBoolean("verified", false)
            }
        }
}

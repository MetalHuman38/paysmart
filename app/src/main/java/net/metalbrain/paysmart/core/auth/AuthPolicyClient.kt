package net.metalbrain.paysmart.core.auth


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuthPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    suspend fun beforeCreate(
        email: String,
        displayName: String? = null,
        tenant: String? = null
    ): AuthDecision = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("eventType", "providers/firebase.auth/eventTypes/beforeCreate:password")
            tenant?.let { put("resource", it) }
            put("data", JSONObject().apply {
                put("userInfo", JSONObject().apply {
                    put("email", email)
                    put("emailVerified", false)
                    put("disabled", false)
                    displayName?.let { put("displayName", it) }
                })
            })
        }

        val body = payload.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(config.beforeCreateUrl)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        parseResponse(response)
    }

    suspend fun beforeSignIn(
        email: String,
        emailVerified: Boolean = true,
        phoneNumber: String? = null
    ): AuthDecision = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("eventType", "providers/firebase.auth/eventTypes/beforeSignIn:password")
            put("data", JSONObject().apply {
                put("userInfo", JSONObject().apply {
                    put("email", email)
                    put("emailVerified", emailVerified)
                    put("disabled", false)
                    phoneNumber?.let { put("phoneNumber", it) }
                })
            })
        }

        val body = payload.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(config.beforeSignInUrl)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        parseResponse(response)
    }

    private fun parseResponse(response: Response): AuthDecision {
        val json = response.body?.string()?.let { JSONObject(it) } ?: return AuthDecision.Deny(
            response.code,
            "empty-response",
            "No content returned from server"
        )

        return if (response.isSuccessful) {
            AuthDecision.Allow(response.code, json.optJSONObject("userRecord"))
        } else {
            val err = json.optJSONObject("error")
            AuthDecision.Deny(
                httpStatus = response.code,
                errorCode = err?.optString("code"),
                errorMessage = err?.optString("message")
            )
        }
    }
}

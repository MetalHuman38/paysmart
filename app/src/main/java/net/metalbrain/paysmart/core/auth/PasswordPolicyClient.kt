package net.metalbrain.paysmart.core.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PasswordPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    /**
     * Mark passwordEnabled=true on server.
     */
    suspend fun markPasswordEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.setPasswordEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        Log.d("PasswordPolicyClient", "Sending request: $request")

        val response = httpClient.newCall(request).execute()
        Log.d("PasswordPolicyClient", "Response: $response")

        response.use {
            it.isSuccessful
        }
    }

    /**
     * Check if server has passwordEnabled=true for this user.
     */
    suspend fun isPasswordEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.getPasswordEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        Log.d("PasswordPolicyClient", "Sending isPasswordEnabled request: $request")

        httpClient.newCall(request).execute().use { response ->
            Log.d("PasswordPolicyClient", "Response: $response")

            if (!response.isSuccessful) {
                Log.w("PasswordPolicyClient", "isPasswordEnabled failed: ${response.code}")
                return@withContext false
            }

            val responseBody = response.body?.string()
            if (responseBody == null) {
                Log.w("PasswordPolicyClient", "isPasswordEnabled: empty body")
                return@withContext false
            }

            try {
                val json = JSONObject(responseBody)
                return@withContext json.optBoolean("passwordEnabled", false)
            } catch (e: Exception) {
                Log.e("PasswordPolicyClient", "isPasswordEnabled: JSON parse error", e)
                return@withContext false
            }
        }

    }
}

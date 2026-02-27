package net.metalbrain.paysmart.core.features.account.authorization.biometric.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BiometricPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Mark BiometricEnabled=true on server.
     */
    suspend fun markBiometricEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.setBiometricEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        if (BuildConfig.DEBUG) {
            Log.d("BiometricPolicyClient", "markBiometricEnabled request dispatched")
        }

        val response = httpClient.newCall(request).execute()
        if (BuildConfig.DEBUG && !response.isSuccessful) {
            Log.w("BiometricPolicyClient", "markBiometricEnabled failed code=${response.code}")
        }

        response.use {
            it.isSuccessful
        }
    }

    /**
     * Check if server has BiometricEnabled=true for this user.
     */
    suspend fun isBiometricEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.getBiometricEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext false
            }

            val responseBody = response.body?.string()
            if (responseBody == null) {
                Log.w("BiometricPolicyClient", "isPasswordEnabled: empty body")
                return@withContext false
            }

            try {
                val json = JSONObject(responseBody)
                return@withContext json.optBoolean("BiometricEnabled", false)
            } catch (e: Exception) {
                Log.e("BiometricPolicyClient", "isBiometricEnabled: JSON parse error", e)
                return@withContext false
            }
        }

    }
}

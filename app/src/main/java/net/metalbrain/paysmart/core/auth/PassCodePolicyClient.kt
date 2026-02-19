package net.metalbrain.paysmart.core.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PassCodePolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    /**
     * Mark passCodeEnabled=true on server.
     */
    suspend fun markPassCodeEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.setPassCodeEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        Log.d("PassCodePolicyClient", "Sending request: $request")

        val response = httpClient.newCall(request).execute()
        Log.d("PassCodePolicyClient", "Response: $response")

        response.use {
            it.isSuccessful
        }
    }

    /**
     * Check if server has passCodeEnabled=true for this user.
     */
    suspend fun isPassCodeEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(config.getPassCodeEnabledUrl)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext false
            }

            val responseBody = response.body?.string()
            if (responseBody == null) {
                Log.w("PassCodePolicyClient", "isPassCodeEnabled: empty body")
                return@withContext false
            }

            try {
                val json = JSONObject(responseBody)
                return@withContext json.optBoolean("PassCodeEnabled", false)
            } catch (e: Exception) {
                Log.e("PassCodePolicyClient", "isPassCodeEnabled: JSON parse error", e)
                return@withContext false
            }
        }

    }
}


//package net.metalbrain.paysmart.core.auth
//
//
//import android.util.Log
//import com.google.firebase.appcheck.FirebaseAppCheck
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONObject
//
//class PassCodePolicyClient(
//    private val config: AuthApiConfig,
//    private val appCheck: FirebaseAppCheck,
//    private val httpClient: OkHttpClient
//) {
//
//    /**
//     * Mark passCodeEnabled = true on server.
//     */
//    suspend fun markPassCodeEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
//        val appCheckToken = appCheck.getAppCheckToken(false).await()
//        val token = appCheckToken?.token
//        if (token.isNullOrBlank()) {
//            Log.w("PassCodePolicyClient", "App Check token is null or empty.")
//            return@withContext false
//        }
//
//        val request = Request.Builder()
//            .url(config.setPassCodeEnabledUrl)
//            .header("Authorization", "Bearer $idToken")
//            .header("X-Firebase-AppCheck", token)
//            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
//            .build()
//        val response = httpClient.newCall(request).execute()
//        response.use {
//            it.isSuccessful
//        }
//    }
//
//    /**
//     * Check if server has passcodeEnabled=true for this user.
//     */
//    suspend fun isPassCodeEnabled(idToken: String): Boolean = withContext(Dispatchers.IO) {
//        val appCheckToken = appCheck.getAppCheckToken(false).await()
//        val token = appCheckToken?.token
//        if (token.isNullOrBlank()) {
//            Log.w("PassCodePolicyClient", "App Check token is null or empty.")
//            return@withContext false
//        }
//
//        val request = Request.Builder()
//            .url(config.getPassCodeEnabledUrl)
//            .header("Authorization", "Bearer $idToken")
//            .header("X-Firebase-AppCheck", token)
//            .get()
//            .build()
//        httpClient.newCall(request).execute().use { response ->
//            if (!response.isSuccessful) {
//                Log.w("PassCodePolicyClient", "isPasscodeEnabled failed: ${response.code}")
//                return@withContext false
//            }
//            val responseBody = response.body?.string()
//            if (responseBody == null) {
//                Log.w("PassCodePolicyClient", "isPasscodeEnabled: empty body")
//                return@withContext false
//            }
//
//            try {
//                val json = JSONObject(responseBody)
//                return@withContext json.optBoolean("passcodeEnabled", false)
//            } catch (e: Exception) {
//                Log.e("PasscodePolicyClient", "isPasscodeEnabled: JSON parse error", e)
//                return@withContext false
//            }
//        }
//
//    }
//}

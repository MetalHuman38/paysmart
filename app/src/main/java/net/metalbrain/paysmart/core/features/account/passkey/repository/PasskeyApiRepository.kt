package net.metalbrain.paysmart.core.features.account.passkey.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachOptionalAppCheckToken
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasskeyApiRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val appCheckTokenProvider: AppCheckTokenProvider
) {
    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchRegistrationOptions(
        userName: String,
        userDisplayName: String
    ): Result<String> = runCatching {
        val body = JSONObject()
            .put("userName", userName)
            .put("userDisplayName", userDisplayName)
            .toString()

        post(
            url = config.passkeyRegisterOptionsUrl,
            body = body
        ) { payload ->
            JSONObject(payload).getJSONObject("options").toString()
        }
    }

    suspend fun verifyRegistration(credentialJson: String): Result<Boolean> = runCatching {
        val body = JSONObject()
            .put("credentialJson", credentialJson)
            .toString()

        post(
            url = config.passkeyRegisterVerifyUrl,
            body = body
        ) { payload ->
            JSONObject(payload).optBoolean("verified", false)
        }
    }

    suspend fun fetchAuthenticationOptions(): Result<String> = runCatching {
        post(
            url = config.passkeyAuthenticateOptionsUrl,
            body = "{}"
        ) { payload ->
            JSONObject(payload).getJSONObject("options").toString()
        }
    }

    suspend fun verifyAuthentication(credentialJson: String): Result<Boolean> = runCatching {
        val body = JSONObject()
            .put("credentialJson", credentialJson)
            .toString()

        post(
            url = config.passkeyAuthenticateVerifyUrl,
            body = body
        ) { payload ->
            JSONObject(payload).optBoolean("verified", false)
        }
    }

    private suspend fun <T> post(
        url: String,
        body: String,
        onSuccess: (String) -> T
    ): T {
        val session = authRepository.getCurrentSessionOrThrow()
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${session.idToken}")
                .attachOptionalAppCheckToken(appCheckTokenProvider)
                .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseErrorMessage(raw, "Passkey request failed"))
                }
                onSuccess(raw)
            }
        }
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return runCatching {
            JSONObject(body).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

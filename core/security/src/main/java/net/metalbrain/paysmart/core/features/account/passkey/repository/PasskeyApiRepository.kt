package net.metalbrain.paysmart.core.features.account.passkey.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachOptionalAppCheckToken
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class PasskeySignInVerificationResult(
    val verified: Boolean,
    val customToken: String,
    val uid: String?,
    val credentialId: String?
)

data class PasskeyCredentialItem(
    val credentialId: String,
    val deviceType: String?,
    val backedUp: Boolean,
    val transports: List<String>,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Singleton
class PasskeyApiRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val appCheckTokenProvider: AppCheckTokenProvider,
    @param:ApiPrefixedAuthConfig private val config: AuthApiConfig
) {
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

    suspend fun fetchSignInOptions(): Result<String> = runCatching {
        post(
            url = config.passkeySignInOptionsUrl,
            body = "{}",
            includeAuth = false
        ) { payload ->
            JSONObject(payload).getJSONObject("options").toString()
        }
    }

    suspend fun verifySignIn(credentialJson: String): Result<PasskeySignInVerificationResult> =
        runCatching {
            val body = JSONObject()
                .put("credentialJson", credentialJson)
                .toString()

            post(
                url = config.passkeySignInVerifyUrl,
                body = body,
                includeAuth = false
            ) { payload ->
                val json = JSONObject(payload)
                PasskeySignInVerificationResult(
                    verified = json.optBoolean("verified", false),
                    customToken = json.optString("customToken").trim(),
                    uid = json.optString("uid").takeIf { it.isNotBlank() },
                    credentialId = json.optString("credentialId").takeIf { it.isNotBlank() }
                )
            }
        }

    suspend fun setPasskeyEnabled(passkeyEnabled: Boolean): Result<Boolean> = runCatching {
        val body = JSONObject()
            .put("passkeyEnabled", passkeyEnabled)
            .toString()

        post(
            url = config.setPasskeyEnabledUrl,
            body = body
        ) { payload ->
            JSONObject(payload).optBoolean("ok", false)
        }
    }

    suspend fun revokeCredential(credentialId: String): Result<Boolean> = runCatching {
        val body = JSONObject()
            .put("credentialId", credentialId)
            .toString()

        post(
            url = config.passkeyRevokeUrl,
            body = body
        ) { payload ->
            JSONObject(payload).optBoolean("revoked", false)
        }
    }

    suspend fun listCredentials(): Result<List<PasskeyCredentialItem>> = runCatching {
        get(
            url = config.passkeyListUrl
        ) { payload ->
            val json = JSONObject(payload)
            val items = json.optJSONArray("credentials")
            if (items == null) {
                emptyList()
            } else {
                List(items.length()) { index ->
                    val item = items.optJSONObject(index) ?: JSONObject()
                    val transportsArray = item.optJSONArray("transports")
                    val transports = if (transportsArray == null) {
                        emptyList()
                    } else {
                        List(transportsArray.length()) { transportIndex ->
                            transportsArray.optString(transportIndex).trim()
                        }.filter { it.isNotBlank() }
                    }

                    PasskeyCredentialItem(
                        credentialId = item.optString("credentialId").trim(),
                        deviceType = item.optString("deviceType").trim().ifBlank { null },
                        backedUp = item.optBoolean("backedUp", false),
                        transports = transports,
                        createdAtMs = item.optLong("createdAtMs", 0L),
                        updatedAtMs = item.optLong("updatedAtMs", 0L)
                    )
                }.filter { it.credentialId.isNotBlank() }
            }
        }
    }

    private suspend fun <T> post(
        url: String,
        body: String,
        includeAuth: Boolean = true,
        onSuccess: (String) -> T
    ): T {
        return withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder()
                .url(url)
                .attachOptionalAppCheckToken(appCheckTokenProvider)
                .post(body.toRequestBody("application/json".toMediaTypeOrNull()))

            if (includeAuth) {
                val session = authRepository.getCurrentSessionOrThrow()
                requestBuilder.header("Authorization", "Bearer ${session.idToken}")
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseErrorMessage(raw, "Passkey request failed"))
                }
                onSuccess(raw)
            }
        }
    }

    private suspend fun <T> get(
        url: String,
        includeAuth: Boolean = true,
        onSuccess: (String) -> T
    ): T {
        return withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder()
                .url(url)
                .attachOptionalAppCheckToken(appCheckTokenProvider)
                .get()

            if (includeAuth) {
                val session = authRepository.getCurrentSessionOrThrow()
                requestBuilder.header("Authorization", "Bearer ${session.idToken}")
            }

            val request = requestBuilder.build()

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
            val json = JSONObject(body)
            val code = json.optString("code").trim()
            when (code) {
                "PASSKEY_NOT_CONFIGURED" -> {
                    "Passkey is not configured on server. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES)."
                }

                else -> json.optString("error", fallback).ifBlank { fallback }
            }
        }.getOrDefault(fallback)
    }
}

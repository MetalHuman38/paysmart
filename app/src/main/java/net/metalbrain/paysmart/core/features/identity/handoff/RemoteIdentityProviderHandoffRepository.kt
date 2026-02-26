package net.metalbrain.paysmart.core.features.identity.handoff

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
class RemoteIdentityProviderHandoffRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val appCheckTokenProvider: AppCheckTokenProvider
) : IdentityProviderHandoffRepository {

    private val config = AuthApiConfig(baseUrl = Env.apiBase, attachApiPrefix = true)
    private val client = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()

    override suspend fun startSession(
        request: IdentityProviderSessionStart
    ): Result<IdentityProviderSession> = runCatching {
        val payload = JSONObject()
            .put("countryIso2", request.countryIso2)
            .put("documentType", request.documentType)
            .toString()
        post(config.identityProviderStartUrl, payload) { json ->
            IdentityProviderSession(
                sessionId = json.optString("sessionId"),
                provider = json.optString("provider", "third_party"),
                status = json.optString("status", "session_created"),
                launchUrl = json.optString("launchUrl").ifBlank { null },
                expiresAtMs = json.optLong("expiresAtMs").takeIf { it > 0 }
            )
        }
    }

    override suspend fun resumeSession(
        sessionId: String
    ): Result<IdentityProviderSessionResume> = runCatching {
        val payload = JSONObject().put("sessionId", sessionId).toString()
        post(config.identityProviderResumeUrl, payload, ::parseResume)
    }

    override suspend fun submitSdkCallback(
        callback: IdentityProviderSdkCallback
    ): Result<IdentityProviderSessionResume> = runCatching {
        val payload = JSONObject()
            .put("event", callback.event)
            .put("sessionId", callback.sessionId)
            .put("providerRef", callback.providerRef)
            .put("rawDeepLink", callback.rawDeepLink)
            .toString()
        post(config.identityProviderCallbackUrl, payload, ::parseResume)
    }

    private suspend fun <T> post(
        url: String,
        payload: String,
        parser: (JSONObject) -> T
    ): T = withContext(Dispatchers.IO) {
        val session = authRepository.getCurrentSessionOrThrow()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${session.idToken}")
            .attachOptionalAppCheckToken(appCheckTokenProvider)
            .post(payload.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.newCall(request).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(parseError(rawBody, "Identity provider handoff failed"))
            }
            parser(JSONObject(rawBody))
        }
    }

    private fun parseResume(json: JSONObject): IdentityProviderSessionResume {
        return IdentityProviderSessionResume(
            sessionId = json.optString("sessionId"),
            provider = json.optString("provider", "third_party"),
            status = json.optString("status", "pending"),
            launchUrl = json.optString("launchUrl").ifBlank { null },
            reason = json.optString("reason").ifBlank { null },
            updatedAtMs = json.optLong("updatedAtMs").takeIf { it > 0 }
        )
    }

    private fun parseError(rawBody: String, fallback: String): String {
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrElse { rawBody.take(220) }
    }
}

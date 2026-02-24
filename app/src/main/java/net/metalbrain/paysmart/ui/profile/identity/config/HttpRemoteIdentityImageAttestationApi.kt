package net.metalbrain.paysmart.ui.profile.identity.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.ui.profile.identity.provider.IdentityImageDecision
import net.metalbrain.paysmart.ui.profile.identity.provider.IdentityImageDetectionResult
import net.metalbrain.paysmart.ui.profile.identity.provider.RemoteIdentityImageAttestationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpRemoteIdentityImageAttestationApi @Inject constructor(
    private val authRepository: AuthRepository
) : RemoteIdentityImageAttestationApi {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun attest(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityImageDetectionResult> = runCatching {
        require(imageBytes.isNotEmpty()) { "Captured image is empty" }
        require(mimeType.isNotBlank()) { "Captured image mimeType is empty" }

        val session = authRepository.getCurrentSessionOrThrow()
        val requestPayload = JSONObject()
            .put("mimeType", mimeType)
            .put("payloadBase64", Base64.getEncoder().encodeToString(imageBytes))
            .put("schemaVersion", 1)
            .toString()

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(config.identityImageAttestationUrl)
                .header("Authorization", "Bearer ${session.idToken}")
                .post(
                    requestPayload.toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(
                        parseErrorMessage(body, "Remote image attestation failed")
                    )
                }

                parseResponse(body)
            }
        }
    }

    private fun parseResponse(raw: String): IdentityImageDetectionResult {
        val json = JSONObject(raw)
        val decisionRaw = json.optString("decision", "clear")
        val decision = when (decisionRaw.trim().lowercase()) {
            "suspected_synthetic",
            "synthetic",
            "ai_generated" -> IdentityImageDecision.SUSPECTED_SYNTHETIC

            else -> IdentityImageDecision.CLEAR
        }

        val confidence = json.optDouble("confidence", Double.NaN)
            .takeIf { it.isFinite() }
            ?.toFloat()

        val provider = json.optString("provider", "remote_attestation_api")
            .ifBlank { "remote_attestation_api" }

        return IdentityImageDetectionResult(
            decision = decision,
            confidence = confidence,
            provider = provider
        )
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return runCatching {
            JSONObject(body).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

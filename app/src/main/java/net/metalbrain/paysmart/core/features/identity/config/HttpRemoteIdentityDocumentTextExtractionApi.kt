package net.metalbrain.paysmart.core.features.identity.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.identity.provider.IdentityDocumentTextExtraction
import net.metalbrain.paysmart.core.features.identity.provider.RemoteIdentityDocumentTextExtractionApi
import net.metalbrain.paysmart.data.repository.AuthRepository
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
class HttpRemoteIdentityDocumentTextExtractionApi @Inject constructor(
    private val authRepository: AuthRepository
) : RemoteIdentityDocumentTextExtractionApi {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun extract(
        imageBytes: ByteArray,
        mimeType: String
    ): Result<IdentityDocumentTextExtraction> = runCatching {
        require(imageBytes.isNotEmpty()) { "Document image is empty" }
        require(mimeType.isNotBlank()) { "Document mimeType is empty" }

        val session = authRepository.getCurrentSessionOrThrow()
        val requestPayload = JSONObject()
            .put("mimeType", mimeType)
            .put("payloadBase64", Base64.getEncoder().encodeToString(imageBytes))
            .toString()

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(config.identityExtractTextUrl)
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
                        parseErrorMessage(body, "Remote document text extraction failed")
                    )
                }

                parseResponse(body)
            }
        }
    }

    private fun parseResponse(raw: String): IdentityDocumentTextExtraction {
        val json = JSONObject(raw)
        return IdentityDocumentTextExtraction(
            fullText = json.optString("fullText", ""),
            candidateFullName = json.optString("candidateFullName", "").ifBlank { null },
            provider = json.optString("provider", "remote_identity_ocr_api").ifBlank {
                "remote_identity_ocr_api"
            }
        )
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return runCatching {
            JSONObject(body).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

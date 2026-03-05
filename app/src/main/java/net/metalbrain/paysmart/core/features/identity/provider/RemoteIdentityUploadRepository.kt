package net.metalbrain.paysmart.core.features.identity.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.auth.appcheck.provider.attachOptionalAppCheckToken
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.EncryptedIdentityPayload
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityDocumentType
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadReceipt
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadRepository
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.IdentityUploadSession
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.util.Base64
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteIdentityUploadRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val appCheckTokenProvider: AppCheckTokenProvider,
    private val performanceMonitor: AppPerformanceMonitor
) : IdentityUploadRepository {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(75, TimeUnit.SECONDS)
        .build()

    override suspend fun createUploadSession(
        documentType: IdentityDocumentType,
        payloadSha256: String,
        contentType: String
    ): Result<IdentityUploadSession> = runCatching {
        performanceMonitor.trace(
            name = "identity_create_upload_session",
            attributes = mapOf(
                "document_type" to documentType.toApiValue(),
                "content_type" to contentType.take(40)
            )
        ) {
            val session = authRepository.getCurrentSessionOrThrow()
            val requestJson = JSONObject()
                .put("documentType", documentType.toApiValue())
                .put("payloadSha256", payloadSha256)
                .put("contentType", contentType)
                .toString()

            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(config.identityUploadSessionUrl)
                    .header("Authorization", "Bearer ${session.idToken}")
                    .attachOptionalAppCheckToken(appCheckTokenProvider)
                    .post(requestJson.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                try {
                    httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            throw IllegalStateException(parseErrorMessage(body, "Unable to create upload session"))
                        }

                        val json = JSONObject(body)
                        val uploadUrl = normalizeUploadUrlForLocal(json.getString("uploadUrl"))
                        IdentityUploadSession(
                            sessionId = json.getString("sessionId"),
                            uploadUrl = uploadUrl,
                            objectPath = json.getString("objectPath"),
                            associatedData = json.getString("associatedData"),
                            attestationNonce = json.getString("attestationNonce"),
                            encryptionKeyBase64 = json.optString("encryptionKeyBase64"),
                            encryptionSchema = json.optString("encryptionSchema", "aes-256-gcm-v1"),
                            cryptoContractVersion = json.optString("cryptoContractVersion", "unknown")
                        )
                    }
                } catch (error: SocketTimeoutException) {
                    throw IllegalStateException("Timeout while creating identity upload session", error)
                }
            }
        }
    }

    override suspend fun uploadEncryptedPayload(
        session: IdentityUploadSession,
        payload: EncryptedIdentityPayload
    ): Result<Unit> = runCatching {
        performanceMonitor.trace(
            name = "identity_upload_encrypted_payload",
            attributes = mapOf(
                "document_size_bytes" to payload.cipherText.size.toString(),
                "local_fallback" to shouldUseLocalPayloadFallback(session.uploadUrl).toString()
            )
        ) {
            if (shouldUseLocalPayloadFallback(session.uploadUrl)) {
                uploadEncryptedPayloadViaFallback(session, payload)
                return@trace
            }

            withContext(Dispatchers.IO) {
                val uploadMediaType = "application/octet-stream".toMediaTypeOrNull()
                val request = Request.Builder()
                    .url(session.uploadUrl)
                    .put(payload.cipherText.toRequestBody(uploadMediaType))
                    .build()

                try {
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            val body = response.body?.string().orEmpty()
                            val detail = parseErrorMessage(body, "Upload failed")
                            throw IllegalStateException(
                                "Unable to upload encrypted payload (http=${response.code}, detail=$detail, host=${request.url.host})"
                            )
                        }
                    }
                } catch (error: SocketTimeoutException) {
                    throw IllegalStateException("Timeout while uploading encrypted payload", error)
                }
            }
        }
    }

    override suspend fun commitUpload(
        sessionId: String,
        payloadSha256: String,
        attestationJwt: String
    ): Result<IdentityUploadReceipt> = runCatching {
        performanceMonitor.trace(
            name = "identity_commit_upload",
            attributes = mapOf("session_id" to sessionId.take(40))
        ) {
            val session = authRepository.getCurrentSessionOrThrow()
            val requestJson = JSONObject()
                .put("sessionId", sessionId)
                .put("payloadSha256", payloadSha256)
                .put("attestationJwt", attestationJwt)
                .toString()

            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(config.identityUploadCommitUrl)
                    .header("Authorization", "Bearer ${session.idToken}")
                    .attachOptionalAppCheckToken(appCheckTokenProvider)
                    .post(requestJson.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                try {
                    httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            throw IllegalStateException(parseErrorMessage(body, "Unable to commit upload"))
                        }

                        val json = JSONObject(body)
                        IdentityUploadReceipt(
                            verificationId = json.getString("verificationId"),
                            status = json.getString("status")
                        )
                    }
                } catch (error: SocketTimeoutException) {
                    throw IllegalStateException("Timeout while committing verification request", error)
                }
            }
        }
    }

    private fun IdentityDocumentType.toApiValue(): String {
        return when (this) {
            IdentityDocumentType.PASSPORT -> "passport"
            IdentityDocumentType.DRIVERS_LICENSE -> "drivers_license"
            IdentityDocumentType.NATIONAL_ID -> "national_id"
        }
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return runCatching {
            val json = JSONObject(body)
            val error = json.optString("error", fallback).ifBlank {
                body.trim().take(220).ifBlank { fallback }
            }
            val code = json.optString("code").trim()
            if (code.isBlank()) error else "$error (code=$code)"
        }.getOrElse {
            body.trim().take(220).ifBlank { fallback }
        }
    }

    private suspend fun uploadEncryptedPayloadViaFallback(
        session: IdentityUploadSession,
        payload: EncryptedIdentityPayload
    ) {
        val currentSession = authRepository.getCurrentSessionOrThrow()
        val requestJson = JSONObject()
            .put("sessionId", session.sessionId)
            .put("payloadBase64", Base64.getEncoder().encodeToString(payload.cipherText))
            .put("contentType", "application/octet-stream")
            .toString()

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(config.identityUploadPayloadUrl)
                .header("Authorization", "Bearer ${currentSession.idToken}")
                .attachOptionalAppCheckToken(appCheckTokenProvider)
                .post(requestJson.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(
                            parseErrorMessage(body, "Unable to upload encrypted payload via local fallback")
                        )
                    }
                }
            } catch (error: SocketTimeoutException) {
                throw IllegalStateException("Timeout while uploading encrypted payload via fallback", error)
            }
        }
    }

    private fun shouldUseLocalPayloadFallback(uploadUrl: String): Boolean {
        if (uploadUrl.isBlank()) return true
        if (!Env.isLocal) return false

        val parsed = uploadUrl.toHttpUrlOrNull() ?: return true
        return parsed.host == "127.0.0.1" ||
            parsed.host == "localhost" ||
            parsed.host == "0.0.0.0" ||
            parsed.host == "::1" ||
            parsed.host == "10.0.2.2"
    }

    private fun normalizeUploadUrlForLocal(rawUrl: String): String {
        if (!Env.isLocal) return rawUrl
        val parsed = rawUrl.toHttpUrlOrNull() ?: return rawUrl
        if (
            parsed.host != "127.0.0.1" &&
            parsed.host != "localhost" &&
            parsed.host != "0.0.0.0" &&
            parsed.host != "::1"
        ) {
            return rawUrl
        }
        return parsed.newBuilder()
            .host("10.0.2.2")
            .build()
            .toString()
    }
}

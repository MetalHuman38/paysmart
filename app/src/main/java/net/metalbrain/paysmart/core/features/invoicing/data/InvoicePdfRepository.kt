package net.metalbrain.paysmart.core.features.invoicing.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoicePdfDocument
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoicePdfRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {
    private val config = AuthApiConfig(baseUrl = Env.apiBase, attachApiPrefix = true)
    private val client = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()

    suspend fun queueGeneration(invoiceId: String): Result<InvoicePdfDocument> = runCatching {
        val session = authRepository.getCurrentSessionOrThrow()
        val request = Request.Builder()
            .url(config.invoiceQueuePdfUrl(invoiceId))
            .header("Authorization", "Bearer ${session.idToken}")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseError(rawBody, "Unable to queue invoice PDF"))
                }
                parsePdfDocument(JSONObject(rawBody))
            }
        }
    }

    suspend fun downloadToShareCache(invoiceId: String, fileName: String): Result<Uri> = runCatching {
        val session = authRepository.getCurrentSessionOrThrow()
        val request = Request.Builder()
            .url(config.invoiceDownloadPdfUrl(invoiceId))
            .header("Authorization", "Bearer ${session.idToken}")
            .get()
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val rawBody = response.body?.string().orEmpty()
                    throw IllegalStateException(parseError(rawBody, "Unable to download invoice PDF"))
                }
                val bytes = response.body?.bytes() ?: throw IllegalStateException("Invoice PDF is empty")
                val invoicesDir = File(context.cacheDir, "invoice-share").apply { mkdirs() }
                val file = File(invoicesDir, fileName)
                file.writeBytes(bytes)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }
    }

    suspend fun enqueueSystemDownload(invoiceId: String, fileName: String): Result<Long> = runCatching {
        val session = authRepository.getCurrentSessionOrThrow()
        val request = DownloadManager.Request(Uri.parse(config.invoiceDownloadPdfUrl(invoiceId)))
            .setMimeType("application/pdf")
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .addRequestHeader("Authorization", "Bearer ${session.idToken}")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun parsePdfDocument(json: JSONObject): InvoicePdfDocument {
        return InvoicePdfDocument(
            status = json.optString("status"),
            fileName = json.optString("fileName"),
            contentType = json.optString("contentType", "application/pdf"),
            templateVersion = json.optString("templateVersion"),
            objectPath = json.optString("objectPath").ifBlank { null },
            sizeBytes = json.optInt("sizeBytes").takeIf { it > 0 },
            generatedAtMs = json.optLong("generatedAtMs").takeIf { it > 0L },
            error = json.optString("error").ifBlank { null }
        )
    }

    private fun parseError(rawBody: String, fallback: String): String {
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

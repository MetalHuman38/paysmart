package net.metalbrain.paysmart.core.features.invoicing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceFinalizeResult
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceFinalizeRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val performanceMonitor: AppPerformanceMonitor
) {
    private val config = AuthApiConfig(baseUrl = Env.apiBase, attachApiPrefix = true)
    private val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()

    suspend fun finalize(
        profile: InvoiceProfileDraft,
        venue: InvoiceVenueDraft,
        weekly: InvoiceWeeklyDraft
    ): Result<InvoiceFinalizeResult> = runCatching {
        performanceMonitor.trace("invoice_finalize") {
            val session = authRepository.getCurrentSessionOrThrow()
            val requestBody = buildRequestBody(profile, venue, weekly)
                .toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(config.invoiceFinalizeUrl)
                .header("Authorization", "Bearer ${session.idToken}")
                .header(
                    "Idempotency-Key",
                    "invoice:${weekly.weekEndingDate}:${weekly.invoiceDate}:${venue.venueId}"
                )
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(parseError(rawBody, "Unable to finalize invoice"))
                    }
                    parseResult(rawBody)
                }
            }
        }
    }

    private fun buildRequestBody(
        profile: InvoiceProfileDraft,
        venue: InvoiceVenueDraft,
        weekly: InvoiceWeeklyDraft
    ): String {
        val shifts = JSONArray(
            weekly.withFullWeek().shifts.map { row ->
                JSONObject()
                    .put("dayLabel", row.dayLabel)
                    .put("workDate", row.workDate)
                    .put("hoursInput", row.hoursInput)
            }
        )

        return JSONObject()
            .put("profile", JSONObject()
                .put("fullName", profile.fullName)
                .put("address", profile.address)
                .put("badgeNumber", profile.badgeNumber)
                .put("badgeExpiryDate", profile.badgeExpiryDate)
                .put("utrNumber", profile.utrNumber)
                .put("email", profile.email)
                .put("contactPhone", profile.contactPhone)
                .put("paymentMethod", profile.paymentMethod.storageKey)
                .put("accountNumber", profile.accountNumber)
                .put("sortCode", profile.sortCode)
                .put("paymentInstructions", profile.paymentInstructions)
                .put("declaration", profile.declaration))
            .put("venue", JSONObject()
                .put("venueId", venue.venueId)
                .put("venueName", venue.venueName)
                .put("venueAddress", venue.venueAddress))
            .put("weekly", JSONObject()
                .put("invoiceDate", weekly.invoiceDate)
                .put("weekEndingDate", weekly.weekEndingDate)
                .put("hourlyRateInput", weekly.hourlyRateInput)
                .put("shifts", shifts))
            .toString()
    }

    private fun parseResult(rawBody: String): InvoiceFinalizeResult {
        val json = JSONObject(rawBody)
        return InvoiceFinalizeResult(
            invoiceId = json.optString("invoiceId"),
            invoiceNumber = json.optString("invoiceNumber"),
            status = json.optString("status"),
            sequenceNumber = json.optInt("sequenceNumber"),
            totalHours = json.optDouble("totalHours"),
            hourlyRate = json.optDouble("hourlyRate"),
            subtotalMinor = json.optInt("subtotalMinor"),
            currency = json.optString("currency", "GBP"),
            venueName = json.optString("venueName"),
            weekEndingDate = json.optString("weekEndingDate"),
            createdAtMs = json.optLong("createdAtMs")
        )
    }

    private fun parseError(rawBody: String, fallback: String): String {
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

package net.metalbrain.paysmart.core.features.invoicing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetail
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoicePdfDocument
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetailProfile
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetailShift
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetailVenue
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceDetailWeekly
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceSummary
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceReadRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val performanceMonitor: AppPerformanceMonitor
) {
    private val config = AuthApiConfig(baseUrl = Env.apiBase, attachApiPrefix = true)
    private val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()

    suspend fun listFinalized(limit: Int = 20, cursor: String? = null): Result<List<InvoiceSummary>> = runCatching {
        performanceMonitor.trace("invoice_list") {
            val session = authRepository.getCurrentSessionOrThrow()
            val request = Request.Builder()
                .url(config.invoiceListUrl(limit, cursor))
                .header("Authorization", "Bearer ${session.idToken}")
                .get()
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(parseError(rawBody, "Unable to load invoices"))
                    }
                    parseList(rawBody)
                }
            }
        }
    }

    suspend fun getById(invoiceId: String): Result<InvoiceDetail> = runCatching {
        performanceMonitor.trace("invoice_detail") {
            val session = authRepository.getCurrentSessionOrThrow()
            val request = Request.Builder()
                .url(config.invoiceByIdUrl(invoiceId))
                .header("Authorization", "Bearer ${session.idToken}")
                .get()
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(parseError(rawBody, "Unable to load invoice detail"))
                    }
                    parseDetail(rawBody)
                }
            }
        }
    }

    private fun parseList(rawBody: String): List<InvoiceSummary> {
        val root = JSONObject(rawBody)
        val items = root.optJSONArray("items") ?: JSONArray()
        return buildList(items.length()) {
            for (index in 0 until items.length()) {
                add(parseSummary(items.optJSONObject(index) ?: JSONObject()))
            }
        }
    }

    private fun parseSummary(json: JSONObject): InvoiceSummary {
        return InvoiceSummary(
            invoiceId = json.optString("invoiceId"),
            invoiceNumber = json.optString("invoiceNumber"),
            status = json.optString("status"),
            subtotalMinor = json.optInt("subtotalMinor"),
            currency = json.optString("currency", "GBP"),
            venueName = json.optString("venueName"),
            weekEndingDate = json.optString("weekEndingDate"),
            createdAtMs = json.optLong("createdAtMs")
        )
    }

    private fun parseDetail(rawBody: String): InvoiceDetail {
        val json = JSONObject(rawBody)
        val profile = json.optJSONObject("profile") ?: JSONObject()
        val venue = json.optJSONObject("venue") ?: JSONObject()
        val weekly = json.optJSONObject("weekly") ?: JSONObject()
        val pdf = json.optJSONObject("pdf") ?: JSONObject()
        val shiftsArray = weekly.optJSONArray("shifts") ?: JSONArray()

        val shifts = buildList(shiftsArray.length()) {
            for (index in 0 until shiftsArray.length()) {
                val row = shiftsArray.optJSONObject(index) ?: JSONObject()
                add(
                    InvoiceDetailShift(
                        dayLabel = row.optString("dayLabel"),
                        workDate = row.optString("workDate"),
                        hoursInput = row.optString("hoursInput")
                    )
                )
            }
        }

        return InvoiceDetail(
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
            createdAtMs = json.optLong("createdAtMs"),
            pdf = InvoicePdfDocument(
                status = pdf.optString("status", "not_requested"),
                fileName = pdf.optString("fileName"),
                contentType = pdf.optString("contentType", "application/pdf"),
                templateVersion = pdf.optString("templateVersion"),
                objectPath = pdf.optString("objectPath").ifBlank { null },
                sizeBytes = pdf.optInt("sizeBytes").takeIf { it > 0 },
                generatedAtMs = pdf.optLong("generatedAtMs").takeIf { it > 0L },
                error = pdf.optString("error").ifBlank { null }
            ),
            profile = InvoiceDetailProfile(
                fullName = profile.optString("fullName"),
                address = profile.optString("address"),
                badgeNumber = profile.optString("badgeNumber"),
                badgeExpiryDate = profile.optString("badgeExpiryDate"),
                utrNumber = profile.optString("utrNumber"),
                email = profile.optString("email"),
                contactPhone = profile.optString("contactPhone"),
                paymentMethod = profile.optString("paymentMethod"),
                accountNumber = profile.optString("accountNumber"),
                sortCode = profile.optString("sortCode"),
                paymentInstructions = profile.optString("paymentInstructions"),
                declaration = profile.optString("declaration")
            ),
            venue = InvoiceDetailVenue(
                venueId = venue.optString("venueId"),
                venueName = venue.optString("venueName"),
                venueAddress = venue.optString("venueAddress")
            ),
            weekly = InvoiceDetailWeekly(
                invoiceDate = weekly.optString("invoiceDate"),
                weekEndingDate = weekly.optString("weekEndingDate"),
                hourlyRateInput = weekly.optString("hourlyRateInput"),
                shifts = shifts
            )
        )
    }

    private fun parseError(rawBody: String, fallback: String): String {
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

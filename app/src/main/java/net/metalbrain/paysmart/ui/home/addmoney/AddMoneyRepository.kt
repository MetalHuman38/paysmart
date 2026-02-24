package net.metalbrain.paysmart.ui.home.addmoney

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
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
class AddMoneyRepository @Inject constructor(
    private val authRepository: AuthRepository
) {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun createSession(amountMinor: Int, currency: String): Result<AddMoneySessionData> = runCatching {
        val session = authRepository.getCurrentSessionOrThrow()
        val requestBody = JSONObject()
            .put("amountMinor", amountMinor)
            .put("currency", currency.uppercase())
            .toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(config.addMoneySessionUrl)
                .header("Authorization", "Bearer ${session.idToken}")
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseError(rawBody, "Unable to create add money session"))
                }

                parseSession(rawBody)
            }
        }
    }

    suspend fun getSessionStatus(sessionId: String): Result<AddMoneySessionData> = runCatching {
        val session = authRepository.getCurrentSessionOrThrow()
        val request = Request.Builder()
            .url(config.addMoneySessionStatusUrl(sessionId))
            .header("Authorization", "Bearer ${session.idToken}")
            .get()
            .build()

        withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseError(rawBody, "Unable to load add money status"))
                }

                parseSession(rawBody)
            }
        }
    }

    private fun parseSession(rawBody: String): AddMoneySessionData {
        if (rawBody.isBlank()) {
            throw IllegalStateException("Add money API returned an empty response")
        }

        val json = JSONObject(rawBody)
        return AddMoneySessionData(
            sessionId = json.optString("sessionId"),
            amountMinor = json.optInt("amountMinor"),
            currency = json.optString("currency", "GBP").uppercase(),
            status = AddMoneySessionStatus.fromRaw(json.optString("status")),
            expiresAtMs = json.optLong("expiresAtMs"),
            paymentIntentId = json.optNullableString("paymentIntentId"),
            paymentIntentClientSecret = json.optNullableString("paymentIntentClientSecret"),
            publishableKey = json.optNullableString("publishableKey")
        )
    }

    private fun parseError(rawBody: String, fallback: String): String {
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key)) return null
    return optString(key).trim().takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}

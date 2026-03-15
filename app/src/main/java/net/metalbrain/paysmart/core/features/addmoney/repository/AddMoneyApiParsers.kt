package net.metalbrain.paysmart.core.features.addmoney.repository

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyErrorCode
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import org.json.JSONObject

internal fun parseAddMoneySession(
    rawBody: String,
    fallbackProvider: AddMoneyProvider
): AddMoneySessionData {
    if (rawBody.isBlank()) {
        throw IllegalStateException("Add money API returned an empty response")
    }

    val json = JSONObject(rawBody)
    val providerRaw = json.optNullableString("provider")
    return AddMoneySessionData(
        sessionId = json.optString("sessionId"),
        amountMinor = json.optInt("amountMinor"),
        currency = json.optString("currency", "GBP").uppercase(),
        status = AddMoneySessionStatus.fromRaw(json.optString("status")),
        expiresAtMs = json.optLong("expiresAtMs"),
        provider = AddMoneyProvider.fromRaw(providerRaw)
            .takeIf { providerRaw != null }
            ?: fallbackProvider,
        checkoutUrl = json.optNullableString("checkoutUrl"),
        flutterwaveTransactionId = json.optNullableString("flutterwaveTransactionId"),
        paymentIntentId = json.optNullableString("paymentIntentId"),
        paymentIntentClientSecret = json.optNullableString("paymentIntentClientSecret"),
        publishableKey = json.optNullableString("publishableKey")
    )
}

internal fun parseAddMoneyApiException(
    statusCode: Int,
    rawBody: String,
    fallbackMessage: String
): AddMoneyApiException {
    if (rawBody.isBlank()) {
        return AddMoneyApiException(
            statusCode = statusCode,
            code = null,
            message = fallbackMessage
        )
    }

    return runCatching {
        val json = JSONObject(rawBody)
        val message = json.optString("error", fallbackMessage).ifBlank { fallbackMessage }
        val code = AddMoneyErrorCode.fromRaw(json.optString("code"))
        AddMoneyApiException(
            statusCode = statusCode,
            code = code,
            message = message
        )
    }.getOrElse {
        AddMoneyApiException(
            statusCode = statusCode,
            code = null,
            message = fallbackMessage
        )
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key)) return null
    return optString(key)
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}

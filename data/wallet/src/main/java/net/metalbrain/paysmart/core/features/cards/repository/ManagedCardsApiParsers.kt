package net.metalbrain.paysmart.core.features.cards.repository

import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardErrorCode
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardStatus
import org.json.JSONArray
import org.json.JSONObject

internal fun parseManagedCards(rawBody: String): List<ManagedCardData> {
    if (rawBody.isBlank()) {
        throw IllegalStateException("Managed cards API returned an empty response")
    }

    val json = JSONObject(rawBody)
    val cards = json.optJSONArray("cards") ?: JSONArray()
    return buildList(cards.length()) {
        for (index in 0 until cards.length()) {
            val card = cards.optJSONObject(index) ?: continue
            add(
                ManagedCardData(
                    id = card.optString("id"),
                    provider = card.optString("provider", "stripe"),
                    brand = card.optString("brand", "card"),
                    last4 = card.optString("last4"),
                    expMonth = card.optInt("expMonth"),
                    expYear = card.optInt("expYear"),
                    funding = card.optNullableString("funding"),
                    country = card.optNullableString("country"),
                    fingerprint = card.optNullableString("fingerprint"),
                    isDefault = card.optBoolean("isDefault"),
                    status = ManagedCardStatus.fromRaw(card.optString("status")),
                    createdAtMs = card.optLong("createdAtMs"),
                    updatedAtMs = card.optLong("updatedAtMs")
                )
            )
        }
    }
}

internal fun parseManagedCardsApiException(
    statusCode: Int,
    rawBody: String,
    fallbackMessage: String
): ManagedCardsApiException {
    if (rawBody.isBlank()) {
        return ManagedCardsApiException(
            statusCode = statusCode,
            code = ManagedCardErrorCode.fromStatusCode(statusCode),
            message = fallbackMessage
        )
    }

    return runCatching {
        val json = JSONObject(rawBody)
        ManagedCardsApiException(
            statusCode = statusCode,
            code = ManagedCardErrorCode.fromStatusCode(statusCode),
            message = json.optString("error", fallbackMessage).ifBlank { fallbackMessage }
        )
    }.getOrElse {
        ManagedCardsApiException(
            statusCode = statusCode,
            code = ManagedCardErrorCode.fromStatusCode(statusCode),
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

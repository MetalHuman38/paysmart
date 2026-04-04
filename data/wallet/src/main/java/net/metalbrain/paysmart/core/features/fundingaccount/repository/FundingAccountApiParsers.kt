package net.metalbrain.paysmart.core.features.fundingaccount.repository

import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountErrorCode
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountProvisionResult
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountProvisioningResult
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountStatus
import org.json.JSONObject

internal fun parseFundingAccount(rawBody: String): FundingAccountData {
    if (rawBody.isBlank()) {
        throw IllegalStateException("Funding account API returned an empty response")
    }

    return parseFundingAccountJson(JSONObject(rawBody))
}

internal fun parseFundingAccountProvisionResult(rawBody: String): FundingAccountProvisionResult {
    if (rawBody.isBlank()) {
        throw IllegalStateException("Funding account provision API returned an empty response")
    }

    val json = JSONObject(rawBody)
    return FundingAccountProvisionResult(
        account = parseFundingAccountJson(json),
        provisioningResult = FundingAccountProvisioningResult.fromRaw(
            json.optString("provisioningResult")
        )
    )
}

internal fun parseFundingAccountApiException(
    statusCode: Int,
    rawBody: String,
    fallbackMessage: String
): FundingAccountApiException {
    if (rawBody.isBlank()) {
        return FundingAccountApiException(
            statusCode = statusCode,
            code = null,
            message = fallbackMessage
        )
    }

    return runCatching {
        val json = JSONObject(rawBody)
        val message = json.optString("error", fallbackMessage).ifBlank { fallbackMessage }
        FundingAccountApiException(
            statusCode = statusCode,
            code = FundingAccountErrorCode.fromRaw(json.optString("code")),
            message = message
        )
    }.getOrElse {
        FundingAccountApiException(
            statusCode = statusCode,
            code = null,
            message = fallbackMessage
        )
    }
}

private fun parseFundingAccountJson(json: JSONObject): FundingAccountData {
    val accountId = json.optString("accountId").trim()
    val provider = json.optString("provider", "flutterwave").trim()
    val currency = json.optString("currency", "NGN").trim().uppercase()
    val accountNumber = json.optString("accountNumber").trim()
    val bankName = json.optString("bankName").trim()
    val accountName = json.optString("accountName").trim()
    val reference = json.optString("reference").trim()
    val providerStatus = json.optString("providerStatus").trim()
        .ifBlank { json.optString("status").trim() }
    val customerId = json.optString("customerId").trim()
    val createdAtMs = json.optLong("createdAtMs")
    val updatedAtMs = json.optLong("updatedAtMs")

    check(accountId.isNotEmpty()) { "Funding account response is missing accountId" }
    check(provider.isNotEmpty()) { "Funding account response is missing provider" }
    check(accountNumber.isNotEmpty()) { "Funding account response is missing accountNumber" }
    check(bankName.isNotEmpty()) { "Funding account response is missing bankName" }
    check(accountName.isNotEmpty()) { "Funding account response is missing accountName" }
    check(reference.isNotEmpty()) { "Funding account response is missing reference" }
    check(customerId.isNotEmpty()) { "Funding account response is missing customerId" }

    return FundingAccountData(
        accountId = accountId,
        provider = provider,
        currency = currency,
        accountNumber = accountNumber,
        bankName = bankName,
        accountName = accountName,
        reference = reference,
        status = FundingAccountStatus.fromRaw(json.optString("status")),
        providerStatus = providerStatus,
        customerId = customerId,
        note = json.optNullableString("note"),
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key)) return null
    return optString(key)
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}

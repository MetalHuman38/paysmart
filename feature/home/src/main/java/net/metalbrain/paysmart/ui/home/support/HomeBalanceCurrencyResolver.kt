package net.metalbrain.paysmart.ui.home.support

import java.util.Locale

fun resolvePrimaryBalanceCurrency(
    balancesByCurrency: Map<String, Double>,
    preferredCurrencyCode: String
): String {
    val normalizedPreferredCurrency = preferredCurrencyCode
        .trim()
        .uppercase(Locale.US)

    if (normalizedPreferredCurrency.isNotBlank()) {
        val matchingBalanceCurrency = balancesByCurrency.keys.firstOrNull { currencyCode ->
            currencyCode.trim().uppercase(Locale.US) == normalizedPreferredCurrency
        }
        if (matchingBalanceCurrency != null) {
            return matchingBalanceCurrency.trim().uppercase(Locale.US)
        }
        if (balancesByCurrency.isEmpty()) {
            return normalizedPreferredCurrency
        }
    }

    if (balancesByCurrency.isEmpty()) {
        return DEFAULT_HOME_BALANCE_CURRENCY
    }

    return balancesByCurrency.keys
        .map { currencyCode -> currencyCode.trim().uppercase(Locale.US) }
        .minOrNull()
        ?: DEFAULT_HOME_BALANCE_CURRENCY
}

fun Map<String, Double>.balanceAmountForCurrency(currencyCode: String): Double {
    val normalizedCurrencyCode = currencyCode.trim().uppercase(Locale.US)
    return entries.firstOrNull { (entryCurrencyCode, _) ->
        entryCurrencyCode.trim().uppercase(Locale.US) == normalizedCurrencyCode
    }?.value ?: 0.0
}

private const val DEFAULT_HOME_BALANCE_CURRENCY = "GBP"

package net.metalbrain.paysmart.core.features.capabilities.catalog

import android.content.Context
import java.util.Locale

object CurrencyFlagResolver {
    private const val DEFAULT_FLAG = "\uD83C\uDFF3\uFE0F"

    fun resolve(
        context: Context,
        currencyCode: String,
        preferredCurrencyCode: String? = null,
        preferredFlagEmoji: String? = null,
        defaultFlagEmoji: String = DEFAULT_FLAG
    ): String {
        val normalizedCurrency = currencyCode.trim().uppercase(Locale.US)
        if (normalizedCurrency.isBlank()) {
            return preferredFlagEmoji.orEmpty().ifBlank { defaultFlagEmoji }
        }

        val normalizedPreferredCurrency = preferredCurrencyCode
            ?.trim()
            ?.uppercase(Locale.US)
            .orEmpty()
        val normalizedPreferredFlag = preferredFlagEmoji.orEmpty()

        if (
            normalizedPreferredCurrency.isNotBlank() &&
            normalizedPreferredFlag.isNotBlank() &&
            normalizedCurrency == normalizedPreferredCurrency
        ) {
            return normalizedPreferredFlag
        }

        val fromCatalog = CountrySelectionCatalog.currencyByCode(
            context = context,
            rawCurrencyCode = normalizedCurrency
        )?.flagEmoji
        if (!fromCatalog.isNullOrBlank()) {
            return fromCatalog
        }

        return normalizedPreferredFlag.ifBlank { defaultFlagEmoji }
    }
}

package net.metalbrain.paysmart.core.features.capabilities.catalog

import android.content.Context
import net.metalbrain.paysmart.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.Currency
import java.util.Locale

data class CountrySelectionItem(
    val iso2: String,
    val name: String,
    val flagEmoji: String,
    val currencyCode: String
)

data class CurrencySelectionItem(
    val code: String,
    val displayName: String,
    val flagEmoji: String
)

object CountrySelectionCatalog {
    @Volatile
    private var countriesCache: List<CountrySelectionItem>? = null

    @Volatile
    private var currenciesCache: List<CurrencySelectionItem>? = null

    fun countries(context: Context): List<CountrySelectionItem> {
        countriesCache?.let { return it }
        synchronized(this) {
            countriesCache?.let { return it }
            val parsed = parseCountries(context)
            countriesCache = parsed
            return parsed
        }
    }

    fun currencies(context: Context): List<CurrencySelectionItem> {
        currenciesCache?.let { return it }
        synchronized(this) {
            currenciesCache?.let { return it }
            val countries = countries(context)
            val result = linkedMapOf<String, CurrencySelectionItem>()
            countries.forEach { country ->
                val code = country.currencyCode.uppercase(Locale.US)
                if (code.length != 3) return@forEach

                val displayName = runCatching {
                    Currency.getInstance(code).getDisplayName(Locale.getDefault())
                }.getOrDefault(code)

                result.putIfAbsent(
                    code,
                    CurrencySelectionItem(
                        code = code,
                        displayName = displayName,
                        flagEmoji = if (code == "EUR") "🇪🇺" else country.flagEmoji
                    )
                )
            }

            val sorted = result.values.sortedBy { it.code }
            currenciesCache = sorted
            return sorted
        }
    }

    fun countryByIso2(context: Context, rawIso2: String): CountrySelectionItem? {
        val iso2 = rawIso2.trim().uppercase(Locale.US)
        if (iso2.length != 2) return null
        return countries(context).firstOrNull { it.iso2 == iso2 }
    }

    fun currencyByCode(context: Context, rawCurrencyCode: String): CurrencySelectionItem? {
        val code = rawCurrencyCode.trim().uppercase(Locale.US)
        if (code.length != 3) return null
        return currencies(context).firstOrNull { it.code == code }
    }

    fun flagForCountry(context: Context, rawIso2: String): String {
        val iso2 = rawIso2.trim().uppercase(Locale.US)
        if (iso2.length != 2) return "🌍"
        return countryByIso2(context, iso2)?.flagEmoji ?: iso2ToFlagEmoji(iso2)
    }

    private fun parseCountries(context: Context): List<CountrySelectionItem> {
        val raw = context.resources
            .openRawResource(R.raw.country_capabilities_catalog)
            .bufferedReader()
            .use { it.readText() }

        val countries = JSONObject(raw).optJSONArray("countries") ?: JSONArray()
        val result = mutableListOf<CountrySelectionItem>()

        for (index in 0 until countries.length()) {
            val country = countries.optJSONObject(index) ?: continue
            val iso2 = country.optString("iso2").trim().uppercase(Locale.US)
            if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) continue

            val countryName = country.optString("name").trim().ifBlank { iso2 }
            val currencyCode = country.optString("currencyCode").trim()
                .uppercase(Locale.US)
                .ifBlank { "GBP" }
            val flagEmoji = country.optString("flag").trim().ifBlank { iso2ToFlagEmoji(iso2) }

            result += CountrySelectionItem(
                iso2 = iso2,
                name = countryName,
                flagEmoji = flagEmoji,
                currencyCode = currencyCode
            )
        }

        return result.sortedBy { it.name }
    }

    private fun iso2ToFlagEmoji(rawIso2: String): String {
        val iso2 = rawIso2.trim().uppercase(Locale.US)
        if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) return "🌍"
        val first = Character.codePointAt(iso2, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(iso2, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }
}

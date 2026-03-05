package net.metalbrain.paysmart.domain.model
import androidx.annotation.Keep

import androidx.annotation.StringRes
import com.google.i18n.phonenumbers.PhoneNumberUtil
import net.metalbrain.paysmart.R
import java.util.Locale

@Keep
data class Country(
    // Default
    val isoCode: String, // e.g., "DE", "GB"
    @get:StringRes @param:StringRes val nameRes: Int,
    val flagEmoji: String,
    val dialCode: String
)

const val DEFAULT_COUNTRY_ISO2 = "GB"

private val validIso2CountryCodes: Set<String> by lazy {
    Locale.getISOCountries()
        .map { it.uppercase(Locale.US) }
        .toSet()
}

fun isValidIso2CountryCode(rawIso2: String?): Boolean {
    val iso2 = rawIso2?.trim()?.uppercase(Locale.US).orEmpty()
    if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) return false
    return iso2 in validIso2CountryCodes
}

fun normalizeCountryIso2(
    rawIso2: String?,
    fallbackIso2: String = DEFAULT_COUNTRY_ISO2
): String {
    val fallback = fallbackIso2
        .trim()
        .uppercase(Locale.US)
        .takeIf(::isValidIso2CountryCode)
        ?: DEFAULT_COUNTRY_ISO2

    val normalized = rawIso2?.trim()?.uppercase(Locale.US).orEmpty()
    return normalized.takeIf(::isValidIso2CountryCode) ?: fallback
}

val supportedCountries: List<Country> by lazy {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val regions = phoneUtil.supportedRegions
        .map { it.uppercase(Locale.US) }
        .filter { it.length == 2 && it.all(Char::isLetter) }
        .distinct()

    val countries = regions.mapNotNull { iso2 ->
        val countryCode = runCatching {
            phoneUtil.getCountryCodeForRegion(iso2)
        }.getOrNull()
        if (countryCode == null || countryCode <= 0) return@mapNotNull null

        Country(
            isoCode = iso2,
            nameRes = R.string.select_country,
            flagEmoji = iso2ToFlagEmoji(iso2),
            dialCode = "+$countryCode"
        )
    }

    countries.sortedWith(
        compareBy(
            { countryDisplayName(it).lowercase(Locale.getDefault()) },
            { it.isoCode }
        )
    )
}

fun countryDisplayName(country: Country, locale: Locale = Locale.getDefault()): String {
    val iso2 = country.isoCode.trim().uppercase(Locale.US)
    if (!isValidIso2CountryCode(iso2)) return iso2
    return runCatching {
        Locale.Builder()
            .setRegion(iso2)
            .build()
            .getDisplayCountry(locale)
    }.getOrDefault(iso2).ifBlank { iso2 }
}

fun matchCountryByInternationalPrefix(
    rawValue: String,
    countries: List<Country> = supportedCountries
): Pair<Country, String>? {
    val trimmed = rawValue.trim()
    if (!trimmed.startsWith("+")) return null

    val compact = "+${trimmed.filter { it.isDigit() }}"
    if (compact.length <= 1) return null

    val matched = countries
        .sortedByDescending { it.dialCode.length }
        .firstOrNull { compact.startsWith(it.dialCode) }
        ?: return null

    val nationalDigits = compact
        .removePrefix(matched.dialCode)
        .filter { it.isDigit() }
        .take(15)

    return matched to nationalDigits
}

private fun iso2ToFlagEmoji(rawIso2: String): String {
    val iso2 = rawIso2.trim().uppercase(Locale.US)
    if (iso2.length != 2 || iso2.any { !it.isLetter() }) return "\uD83C\uDFF3"
    val first = Character.codePointAt(iso2, 0) - 'A'.code + 0x1F1E6
    val second = Character.codePointAt(iso2, 1) - 'A'.code + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

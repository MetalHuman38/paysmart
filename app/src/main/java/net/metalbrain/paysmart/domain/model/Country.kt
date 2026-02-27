package net.metalbrain.paysmart.domain.model
import androidx.annotation.Keep

import androidx.annotation.StringRes
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

val supportedCountries = listOf(
    Country("GB", R.string.country_uk, iso2ToFlagEmoji("GB"), "+44"),
    Country("US", R.string.country_us, iso2ToFlagEmoji("US"), "+1"),
    Country("DE", R.string.country_germany, iso2ToFlagEmoji("DE"), "+49"),
    Country("FR", R.string.country_france, iso2ToFlagEmoji("FR"), "+33"),
    Country("ES", R.string.country_spain, iso2ToFlagEmoji("ES"), "+34"),
    Country("CN", R.string.country_china, iso2ToFlagEmoji("CN"), "+86"),
    Country("PT", R.string.country_portugal, iso2ToFlagEmoji("PT"), "+351"),
    Country("JP", R.string.country_japan, iso2ToFlagEmoji("JP"), "+81"),
    Country("KR", R.string.country_korea, iso2ToFlagEmoji("KR"), "+82"),
    Country("IT", R.string.country_italy, iso2ToFlagEmoji("IT"), "+39")
)

private fun iso2ToFlagEmoji(rawIso2: String): String {
    val iso2 = rawIso2.trim().uppercase(Locale.US)
    if (iso2.length != 2 || iso2.any { !it.isLetter() }) return "\uD83C\uDFF3"
    val first = Character.codePointAt(iso2, 0) - 'A'.code + 0x1F1E6
    val second = Character.codePointAt(iso2, 1) - 'A'.code + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

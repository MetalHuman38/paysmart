package net.metalbrain.paysmart.domain.model

import java.util.Locale

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

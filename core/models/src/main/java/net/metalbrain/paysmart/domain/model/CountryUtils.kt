package net.metalbrain.paysmart.domain.model

import android.content.Context
import android.telephony.TelephonyManager
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

fun detectDeviceCountryIso2(
    context: Context,
    fallbackIso2: String = DEFAULT_COUNTRY_ISO2
): String {
    val telephonyIso = runCatching {
        val telephony = context.getSystemService(TelephonyManager::class.java)
        val simIso = telephony?.simCountryIso?.trim()?.uppercase(Locale.US)
        val networkIso = telephony?.networkCountryIso?.trim()?.uppercase(Locale.US)
        listOf(simIso, networkIso).firstOrNull { isValidIso2CountryCode(it) }
    }.getOrNull()

    if (isValidIso2CountryCode(telephonyIso)) {
        return telephonyIso!!
    }

    val localeIso = Locale.getDefault().country.trim().uppercase(Locale.US)
    return normalizeCountryIso2(localeIso, fallbackIso2)
}

package net.metalbrain.paysmart.domain.model

import java.util.Locale

enum class LaunchInterest(val rawValue: String) {
    INVOICE("invoice"),
    TOP_UP("top_up");

    companion object {
        fun fromRaw(raw: String?): LaunchInterest? {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.rawValue == normalized }
        }

        fun defaultForCountry(countryIso2: String?): LaunchInterest {
            return if (normalizeCountryIso2ForLaunchInterest(countryIso2) == DEFAULT_LAUNCH_INTEREST_COUNTRY_ISO2) {
                INVOICE
            } else {
                TOP_UP
            }
        }
    }
}

private const val DEFAULT_LAUNCH_INTEREST_COUNTRY_ISO2 = "GB"

private fun normalizeCountryIso2ForLaunchInterest(rawIso2: String?): String {
    val normalized = rawIso2?.trim()?.uppercase(Locale.US).orEmpty()
    return if (normalized.length == 2 && normalized.all { it in 'A'..'Z' }) {
        normalized
    } else {
        DEFAULT_LAUNCH_INTEREST_COUNTRY_ISO2
    }
}

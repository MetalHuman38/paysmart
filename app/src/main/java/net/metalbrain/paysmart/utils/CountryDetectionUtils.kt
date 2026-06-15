package net.metalbrain.paysmart.utils

import android.content.Context
import android.telephony.TelephonyManager
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.isValidIso2CountryCode
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import java.util.Locale

/**
 * Detects the device's country ISO 3166-1 alpha-2 code.
 *
 * The detection priority is as follows:
 * 1. SIM card country ISO from [TelephonyManager].
 * 2. Network provider country ISO from [TelephonyManager].
 * 3. System default [Locale] country.
 *
 * @param context The [Context] used to access the [TelephonyManager] service.
 * @param fallbackIso2 The default country code to return if the detected code is invalid or unavailable.
 * Defaults to [DEFAULT_COUNTRY_ISO2].
 * @return A validated, uppercase 2-letter country code.
 */
fun detectDeviceCountryIso2(
    context: Context,
    fallbackIso2: String = DEFAULT_COUNTRY_ISO2
): String {
    val telephonyIso = runCatching {
        val telephony =
            context.getSystemService(TelephonyManager::class.java)

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

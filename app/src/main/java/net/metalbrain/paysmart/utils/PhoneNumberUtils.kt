package net.metalbrain.paysmart.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException

fun formatPhoneNumberForDisplay(rawNumber: String, dialCode: String): String {
    return try {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val regionCode = phoneUtil.getRegionCodeForCountryCode(dialCode.filter { it.isDigit() }.toInt())

        val numberProto = phoneUtil.parse(rawNumber, regionCode)
        phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    } catch (e: NumberParseException) {
        e.printStackTrace()
        "+$dialCode $rawNumber" // fallback to international format
    }
}

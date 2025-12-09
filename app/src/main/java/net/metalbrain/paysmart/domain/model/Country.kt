package net.metalbrain.paysmart.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.metalbrain.paysmart.R

data class Country(
    // Default
    val isoCode: String, // e.g., "de", "gb"
    @get:StringRes @param:StringRes val nameRes: Int,
    @get:DrawableRes @param:DrawableRes val flagRes: Int,
    val dialCode: String
)

val supportedCountries = listOf(
    Country("gb", R.string.country_uk, R.drawable.flag_uk, "+44"),
    Country("us", R.string.country_us, R.drawable.flag_us, "+1"),
    Country("de", R.string.country_germany, R.drawable.flag_de, "+49"),
    Country("fr", R.string.country_france, R.drawable.flag_fr, "+33"),
    Country("es", R.string.country_spain, R.drawable.flag_es, "+34"),
    Country("zh", R.string.country_china, R.drawable.flag_zh, "+86"),
    Country("pt", R.string.country_portugal, R.drawable.flag_pt, "+351"),
    Country("jp", R.string.country_japan, R.drawable.flag_jp, "+81"),
    Country("ko", R.string.country_korea, R.drawable.flag_ko, "+82"),
    Country("it", R.string.country_italy, R.drawable.flag_it, "+39")
)

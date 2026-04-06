package net.metalbrain.paysmart.domain.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import net.metalbrain.paysmart.core.ui.R

@Keep
data class Language(
    val code: String,
    @get:StringRes @param:StringRes val nameRes: Int,
    val countryIso2: String,
    val isRtl: Boolean = false
)

val supportedLanguages = listOf(
    Language("en", R.string.lang_english_uk, "GB"),
    Language("en-US", R.string.lang_english_us, "US"),
    Language("de", R.string.lang_german, "DE"),
    Language("fr", R.string.lang_french, "FR"),
    Language("zh", R.string.lang_chinese, "CN"),
    Language("pt", R.string.lang_portuguese, "PT"),
    Language("es", R.string.lang_spanish, "ES"),
    Language("it", R.string.lang_italian, "IT"),
    Language("ja", R.string.lang_japanese, "JP"),
    Language("ko", R.string.lang_korean, "KR")
)

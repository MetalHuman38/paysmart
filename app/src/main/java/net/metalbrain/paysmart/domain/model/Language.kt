package net.metalbrain.paysmart.domain.model
import androidx.annotation.Keep

import androidx.annotation.StringRes
import net.metalbrain.paysmart.R

@Keep
data class Language(
    val code: String,
    @get:StringRes @param:StringRes val nameRes: Int,
    val flagResId: Int,
    val isRtl: Boolean = false
)

val supportedLanguages = listOf(
    Language("en", R.string.lang_english_uk, R.drawable.flag_uk),
    Language("en-US", R.string.lang_english_us, R.drawable.flag_us),
    Language("de", R.string.lang_german, R.drawable.flag_de),
    Language("fr", R.string.lang_french, R.drawable.flag_fr),
    Language("zh", R.string.lang_chinese, R.drawable.flag_zh),
    Language("pt", R.string.lang_portuguese, R.drawable.flag_pt),
    Language("es", R.string.lang_spanish, R.drawable.flag_es),
    Language("it", R.string.lang_italian, R.drawable.flag_it),
    Language("ja", R.string.lang_japanese, R.drawable.flag_jp),
    Language("ko", R.string.lang_korean, R.drawable.flag_ko)

)

package net.metalbrain.paysmart.domain.model

import net.metalbrain.paysmart.R

data class Language(
    val code: String,
    val name: String,
    val flagResId: Int,
    val isRtl: Boolean = false
)

val supportedLanguages = listOf(
    Language("en-US", "English (United States)", R.drawable.flag_us),
    Language("en", "English (United Kingdom)", R.drawable.flag_uk),
    Language("zh", "Chinese (中文)", R.drawable.flag_zh),
    Language("fr", "French (Français)", R.drawable.flag_fr),
    Language("de", "German (Deutsch)", R.drawable.flag_de),
    Language("it", "Italian (Italiano)", R.drawable.flag_it),
    Language("pt", "Portuguese (Português)", R.drawable.flag_pt),
    Language("es", "Spanish (Español)", R.drawable.flag_es),
    Language("ko", "Korean (한국어)", R.drawable.flag_ko),
    Language("ja", "Japanese (日本語)", R.drawable.flag_jp),

)

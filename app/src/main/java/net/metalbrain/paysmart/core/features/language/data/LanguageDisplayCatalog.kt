package net.metalbrain.paysmart.core.features.language.data

import androidx.annotation.StringRes
import androidx.compose.ui.unit.LayoutDirection
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.domain.model.supportedLanguages

data class LanguageDisplaySpec(
    val code: String,
    @get:StringRes val nameRes: Int,
    val countryIso2: String,
    val isRtl: Boolean
)

fun resolveLanguageDisplaySpec(code: String): LanguageDisplaySpec {
    val defaultIso2 = CountryCapabilityCatalog.defaultProfile().iso2
    val defaultLanguage = supportedLanguages.firstOrNull { language ->
        language.countryIso2.equals(defaultIso2, ignoreCase = true)
    } ?: supportedLanguages.first()

    val normalized = code.trim()
    val resolvedLanguage = supportedLanguages.firstOrNull { language ->
        language.code.equals(normalized, ignoreCase = true)
    } ?: supportedLanguages.firstOrNull { language ->
        !language.code.contains("-") &&
            language.code.equals(normalized.substringBefore('-'), ignoreCase = true)
    } ?: defaultLanguage

    return LanguageDisplaySpec(
        code = resolvedLanguage.code,
        nameRes = resolvedLanguage.nameRes,
        countryIso2 = resolvedLanguage.countryIso2.ifBlank { defaultIso2 },
        isRtl = resolvedLanguage.isRtl
    )
}

fun layoutDirectionFor(language: Language): LayoutDirection {
    return if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}

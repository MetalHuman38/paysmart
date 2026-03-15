package net.metalbrain.paysmart.core.features.account.profile.util

import androidx.annotation.StringRes
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.language.data.LanguageDisplaySpec
import net.metalbrain.paysmart.core.features.language.data.resolveLanguageDisplaySpec
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode

internal fun accountInformationLanguageLabel(code: String): LanguageDisplaySpec {
    return resolveLanguageDisplaySpec(code)
}

@StringRes
internal fun accountInformationThemeModeRes(mode: AppThemeMode): Int {
    return when (mode) {
        AppThemeMode.SYSTEM -> R.string.theme_mode_system
        AppThemeMode.LIGHT -> R.string.theme_mode_light
        AppThemeMode.DARK -> R.string.theme_mode_dark
    }
}

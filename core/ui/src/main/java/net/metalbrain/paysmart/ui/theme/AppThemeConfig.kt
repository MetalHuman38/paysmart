package net.metalbrain.paysmart.ui.theme

import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode

/**
 * Unified theme configuration model for the two supported app themes.
 */
data class AppThemeConfig(
    val mode: AppThemeMode = AppThemeMode.DARK,
)

package net.metalbrain.paysmart.ui.theme

import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
import net.metalbrain.paysmart.core.features.theme.data.AppThemeVariant

/**
 * Unified theme configuration model. Replaces passing variant + mode as separate
 * parameters throughout the app.
 *
 * Pass this to [PaysmartTheme] to drive the full theme resolution.
 */
data class AppThemeConfig(
    val variant: AppThemeVariant = AppThemeVariant.OBSIDIAN,
    val mode: AppThemeMode = AppThemeMode.DARK,
    val dynamicColorEnabled: Boolean = false,
)

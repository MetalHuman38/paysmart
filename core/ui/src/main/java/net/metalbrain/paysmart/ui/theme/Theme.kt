package net.metalbrain.paysmart.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
import net.metalbrain.paysmart.core.features.theme.data.AppThemeVariant
import net.metalbrain.paysmart.ui.theme.tokens.DSBorderTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSElevationTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSHeightTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSMotionTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSRadiusTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSSpacingTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSTypographyTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSWidthTokens
import net.metalbrain.paysmart.ui.theme.tokens.LocalDSColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.LocalDSTypographyTokens

@SuppressLint("ObsoleteSdkInt")
@Composable
fun PaysmartTheme(
    config: AppThemeConfig = AppThemeConfig(),
    content: @Composable () -> Unit
) {
    val themePack = appThemePackFor(config.variant)
    val useDarkTheme = when (config.mode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        config.dynamicColorEnabled &&
            themePack.supportsDynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> themePack.darkColors
        else -> themePack.lightColors
    }

    val colorTokens = if (useDarkTheme) themePack.darkColorTokens else themePack.lightColorTokens

    CompositionLocalProvider(
        LocalAppThemePack provides themePack,
        LocalDSColorTokens provides colorTokens,
        LocalDSTypographyTokens provides themePack.typographyTokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = themePack.typography,
            shapes = themePack.shapes,
            content = content
        )
    }
}

/**
 * Legacy overload — accepts the pre-Phase-2A parameters so existing call sites in
 * [app] continue to compile without modification.
 *
 * New code should call [PaysmartTheme] with an [AppThemeConfig] directly.
 */
@Composable
fun PaysmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeVariant: AppThemeVariant = AppThemeVariant.PAYSMART,
    content: @Composable () -> Unit
) {
    PaysmartTheme(
        config = AppThemeConfig(
            variant = themeVariant,
            mode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
            dynamicColorEnabled = dynamicColor,
        ),
        content = content,
    )
}

/**
 * Accessor object for design tokens — use inside any composable wrapped by [PaysmartTheme].
 *
 * Usage:
 * ```
 * val colors = PaysmartTheme.colorTokens
 * val spacing = PaysmartTheme.spacing
 * ```
 */
object PaysmartTheme {
    val colorTokens: DSColorTokens
        @Composable @ReadOnlyComposable get() = LocalDSColorTokens.current

    val typographyTokens: DSTypographyTokens
        @Composable @ReadOnlyComposable get() = LocalDSTypographyTokens.current

    // Static tokens — do not vary by theme variant or mode
    val spacing: DSSpacingTokens get() = DSSpacingTokens
    val radius: DSRadiusTokens get() = DSRadiusTokens
    val elevation: DSElevationTokens get() = DSElevationTokens
    val border: DSBorderTokens get() = DSBorderTokens
    val motion: DSMotionTokens get() = DSMotionTokens
    val width: DSWidthTokens get() = DSWidthTokens
    val height: DSHeightTokens get() = DSHeightTokens
}

package net.metalbrain.paysmart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
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

@Composable
fun PaysmartTheme(
    config: AppThemeConfig = AppThemeConfig(),
    content: @Composable () -> Unit
) {
    val useDarkTheme = config.mode == AppThemeMode.DARK
    val colorScheme = if (useDarkTheme) {
        PaySmartThemePack.darkColors
    } else {
        PaySmartThemePack.lightColors
    }
    val colorTokens = if (useDarkTheme) {
        PaySmartThemePack.darkColorTokens
    } else {
        PaySmartThemePack.lightColorTokens
    }

    CompositionLocalProvider(
        LocalAppThemePack provides PaySmartThemePack,
        LocalDSColorTokens provides colorTokens,
        LocalDSTypographyTokens provides PaySmartThemePack.typographyTokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PaySmartThemePack.typography,
            shapes = PaySmartThemePack.shapes,
            content = content
        )
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun PaysmartTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PaysmartTheme(
        config = AppThemeConfig(
            mode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        ),
        content = content,
    )
}

object PaysmartTheme {
    val colorTokens: DSColorTokens
        @Composable @ReadOnlyComposable get() = LocalDSColorTokens.current

    val typographyTokens: DSTypographyTokens
        @Composable @ReadOnlyComposable get() = LocalDSTypographyTokens.current

    val spacing: DSSpacingTokens get() = DSSpacingTokens
    val radius: DSRadiusTokens get() = DSRadiusTokens
    val elevation: DSElevationTokens get() = DSElevationTokens
    val border: DSBorderTokens get() = DSBorderTokens
    val motion: DSMotionTokens get() = DSMotionTokens
    val width: DSWidthTokens get() = DSWidthTokens
    val height: DSHeightTokens get() = DSHeightTokens
}

package net.metalbrain.paysmart.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import net.metalbrain.paysmart.ui.theme.tokens.DSColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSTypographyTokens
import net.metalbrain.paysmart.ui.theme.tokens.PaySmartDarkColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.PaySmartLightColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.PaySmartTypographyTokens

@Immutable
data class AppBackgroundPalette(
    val start: Color,
    val accentOne: Color,
    val accentTwo: Color,
    val end: Color
)

@Immutable
data class AppButtonStyle(
    val useFullPillButtons: Boolean,
    val ghostBorderAlpha: Float
)

@Immutable
data class AppSecurityStyle(
    val useEditorialLayout: Boolean,
    val useGlassPanels: Boolean,
    val ghostBorderAlpha: Float,
    val focusedInputScale: Float,
    val glassPanelAlpha: Float,
    val outerHorizontalPadding: androidx.compose.ui.unit.Dp
)

@Immutable
data class AppThemePack(
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val typography: Typography = Typography,
    val shapes: Shapes = PaySmartShapes,
    val buttonStyle: AppButtonStyle,
    val securityStyle: AppSecurityStyle,
    val lightBackground: AppBackgroundPalette,
    val darkBackground: AppBackgroundPalette,
    val lightColorTokens: DSColorTokens,
    val darkColorTokens: DSColorTokens,
    val typographyTokens: DSTypographyTokens,
)

private val PaySmartLightColorScheme = lightColorScheme(
    primary = PaySmartLightColorTokens.brandPrimary,
    onPrimary = PaySmartLightColorTokens.buttonPrimaryForeground,
    primaryContainer = PaySmartLightColorTokens.fillHover,
    onPrimaryContainer = PaySmartLightColorTokens.textPrimary,
    secondary = PaySmartLightColorTokens.brandSecondary,
    onSecondary = PaySmartLightColorTokens.textInverse,
    secondaryContainer = PaySmartLightColorTokens.buttonSecondaryBackground,
    onSecondaryContainer = PaySmartLightColorTokens.textPrimary,
    tertiary = PaySmartLightColorTokens.brandAccent,
    onTertiary = PaySmartLightColorTokens.textInverse,
    background = PaySmartLightColorTokens.backgroundPrimary,
    onBackground = PaySmartLightColorTokens.textPrimary,
    surface = PaySmartLightColorTokens.surfacePrimary,
    onSurface = PaySmartLightColorTokens.textPrimary,
    surfaceVariant = PaySmartLightColorTokens.surfaceElevated,
    onSurfaceVariant = PaySmartLightColorTokens.textSecondary,
    outline = PaySmartLightColorTokens.borderStrong,
    outlineVariant = PaySmartLightColorTokens.borderSubtle,
    error = PaySmartLightColorTokens.error,
    errorContainer = PaySmartErrorContainer,
    onError = PaySmartLightColorTokens.textInverse,
    onErrorContainer = PaySmartOnErrorContainer
)

private val PaySmartDarkColorScheme = darkColorScheme(
    primary = PaySmartDarkColorTokens.brandPrimary,
    onPrimary = PaySmartDarkColorTokens.buttonPrimaryForeground,
    primaryContainer = PaySmartDarkColorTokens.fillHover,
    onPrimaryContainer = PaySmartDarkColorTokens.textPrimary,
    secondary = PaySmartDarkColorTokens.brandSecondary,
    onSecondary = PaySmartDarkColorTokens.backgroundPrimary,
    secondaryContainer = PaySmartDarkColorTokens.buttonSecondaryBackground,
    onSecondaryContainer = PaySmartDarkColorTokens.textPrimary,
    tertiary = PaySmartDarkColorTokens.brandAccent,
    onTertiary = PaySmartDarkColorTokens.backgroundPrimary,
    background = PaySmartDarkColorTokens.backgroundPrimary,
    onBackground = PaySmartDarkColorTokens.textPrimary,
    surface = PaySmartDarkColorTokens.surfacePrimary,
    onSurface = PaySmartDarkColorTokens.textPrimary,
    surfaceVariant = PaySmartDarkColorTokens.surfaceElevated,
    onSurfaceVariant = PaySmartDarkColorTokens.textSecondary,
    outline = PaySmartDarkColorTokens.borderStrong,
    outlineVariant = PaySmartDarkColorTokens.borderSubtle,
    error = PaySmartDarkColorTokens.error,
    errorContainer = Color(0xFF8C1D18),
    onError = PaySmartDarkColorTokens.textInverse,
    onErrorContainer = Color(0xFFFFDAD6)
)

internal val PaySmartThemePack = AppThemePack(
    lightColors = PaySmartLightColorScheme,
    darkColors = PaySmartDarkColorScheme,
    typography = PaySmartTypography,
    shapes = PaySmartShapes,
    buttonStyle = AppButtonStyle(
        useFullPillButtons = false,
        ghostBorderAlpha = 0.20f
    ),
    securityStyle = AppSecurityStyle(
        useEditorialLayout = false,
        useGlassPanels = false,
        ghostBorderAlpha = 0.20f,
        focusedInputScale = 1f,
        glassPanelAlpha = 1f,
        outerHorizontalPadding = Dimens.screenPadding
    ),
    lightBackground = AppBackgroundPalette(
        start = PaySmartLightColorTokens.backgroundPrimary,
        accentOne = PaySmartLightColorTokens.backgroundPrimary,
        accentTwo = PaySmartLightColorTokens.backgroundPrimary,
        end = PaySmartLightColorTokens.backgroundPrimary
    ),
    darkBackground = AppBackgroundPalette(
        start = PaySmartDarkColorTokens.backgroundPrimary,
        accentOne = PaySmartDarkColorTokens.backgroundPrimary,
        accentTwo = PaySmartDarkColorTokens.backgroundPrimary,
        end = PaySmartDarkColorTokens.backgroundPrimary
    ),
    lightColorTokens = PaySmartLightColorTokens,
    darkColorTokens = PaySmartDarkColorTokens,
    typographyTokens = PaySmartTypographyTokens,
)

val LocalAppThemePack = staticCompositionLocalOf { PaySmartThemePack }

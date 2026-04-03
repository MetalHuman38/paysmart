package net.metalbrain.paysmart.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.theme.data.AppThemeVariant
import net.metalbrain.paysmart.ui.theme.tokens.DSColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSTypographyTokens
import net.metalbrain.paysmart.ui.theme.tokens.ObsidianDarkColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.ObsidianLightColorTokens
import net.metalbrain.paysmart.ui.theme.tokens.ObsidianTypographyTokens
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
    val usePrimaryGradient: Boolean,
    val useFullPillButtons: Boolean,
    val ghostBorderAlpha: Float,
    val primaryGlowAlpha: Float
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
    val variant: AppThemeVariant,
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val typography: Typography = Typography,
    val shapes: Shapes = PaySmartShapes,
    val supportsDynamicColor: Boolean = false,
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

private val ObsidianLightColorScheme = lightColorScheme(
    primary = ObsidianLightColorTokens.brandPrimary,
    onPrimary = ObsidianLightColorTokens.buttonPrimaryForeground,
    primaryContainer = ObsidianLightColorTokens.fillHover,
    onPrimaryContainer = ObsidianLightColorTokens.textPrimary,
    secondary = ObsidianLightColorTokens.brandSecondary,
    onSecondary = ObsidianLightColorTokens.textInverse,
    secondaryContainer = ObsidianLightColorTokens.buttonSecondaryBackground,
    onSecondaryContainer = ObsidianLightColorTokens.textPrimary,
    tertiary = ObsidianLightColorTokens.brandAccent,
    onTertiary = ObsidianLightColorTokens.textInverse,
    background = ObsidianLightColorTokens.backgroundPrimary,
    onBackground = ObsidianLightColorTokens.textPrimary,
    surface = ObsidianLightColorTokens.surfacePrimary,
    onSurface = ObsidianLightColorTokens.textPrimary,
    surfaceVariant = ObsidianLightColorTokens.surfaceElevated,
    onSurfaceVariant = ObsidianLightColorTokens.textSecondary,
    outline = ObsidianLightColorTokens.borderStrong,
    outlineVariant = ObsidianLightColorTokens.borderSubtle,
    error = ObsidianLightColorTokens.error,
    errorContainer = PaySmartErrorContainer,
    onError = ObsidianLightColorTokens.textInverse,
    onErrorContainer = PaySmartOnErrorContainer
)

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianDarkColorTokens.brandPrimary,
    onPrimary = ObsidianDarkColorTokens.buttonPrimaryForeground,
    primaryContainer = ObsidianDarkColorTokens.fillHover,
    onPrimaryContainer = ObsidianDarkColorTokens.textPrimary,
    secondary = ObsidianDarkColorTokens.brandSecondary,
    onSecondary = ObsidianDarkColorTokens.backgroundPrimary,
    secondaryContainer = ObsidianDarkColorTokens.buttonSecondaryBackground,
    onSecondaryContainer = ObsidianDarkColorTokens.textPrimary,
    tertiary = ObsidianDarkColorTokens.brandAccent,
    onTertiary = ObsidianDarkColorTokens.backgroundPrimary,
    background = ObsidianDarkColorTokens.backgroundPrimary,
    onBackground = ObsidianDarkColorTokens.textPrimary,
    surface = ObsidianDarkColorTokens.surfacePrimary,
    onSurface = ObsidianDarkColorTokens.textPrimary,
    surfaceVariant = ObsidianDarkColorTokens.surfaceElevated,
    onSurfaceVariant = ObsidianDarkColorTokens.textSecondary,
    outline = ObsidianDarkColorTokens.borderStrong,
    outlineVariant = ObsidianDarkColorTokens.borderSubtle,
    error = ObsidianDarkColorTokens.error,
    errorContainer = Color(0xFF8C1D18),
    onError = ObsidianDarkColorTokens.textInverse,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val PaySmartThemePack = AppThemePack(
    variant = AppThemeVariant.PAYSMART,
    lightColors = PaySmartLightColorScheme,
    darkColors = PaySmartDarkColorScheme,
    typography = PaySmartTypography,
    shapes = PaySmartShapes,
    supportsDynamicColor = true,
    buttonStyle = AppButtonStyle(
        usePrimaryGradient = false,
        useFullPillButtons = false,
        ghostBorderAlpha = 0.24f,
        primaryGlowAlpha = 0f
    ),
    securityStyle = AppSecurityStyle(
        useEditorialLayout = false,
        useGlassPanels = false,
        ghostBorderAlpha = 0.24f,
        focusedInputScale = 1f,
        glassPanelAlpha = 1f,
        outerHorizontalPadding = Dimens.screenPadding
    ),
    lightBackground = AppBackgroundPalette(
        start = PaySmartLightColorTokens.backgroundGradientTop,
        accentOne = PaySmartLightColorTokens.backgroundGradientMiddle,
        accentTwo = PaySmartLightColorTokens.surfaceElevated,
        end = PaySmartLightColorTokens.backgroundGradientBottom
    ),
    darkBackground = AppBackgroundPalette(
        start = PaySmartDarkColorTokens.backgroundGradientTop,
        accentOne = PaySmartDarkColorTokens.backgroundGradientMiddle,
        accentTwo = PaySmartDarkColorTokens.surfaceElevated,
        end = PaySmartDarkColorTokens.backgroundGradientBottom
    ),
    lightColorTokens = PaySmartLightColorTokens,
    darkColorTokens = PaySmartDarkColorTokens,
    typographyTokens = PaySmartTypographyTokens,
)

private val ObsidianThemePack = AppThemePack(
    variant = AppThemeVariant.OBSIDIAN,
    lightColors = ObsidianLightColorScheme,
    darkColors = ObsidianDarkColorScheme,
    typography = ObsidianTypography,
    shapes = PaySmartShapes,
    supportsDynamicColor = false,
    buttonStyle = AppButtonStyle(
        usePrimaryGradient = true,
        useFullPillButtons = true,
        ghostBorderAlpha = 0.15f,
        primaryGlowAlpha = 0.30f
    ),
    securityStyle = AppSecurityStyle(
        useEditorialLayout = true,
        useGlassPanels = true,
        ghostBorderAlpha = 0.15f,
        focusedInputScale = 1.02f,
        glassPanelAlpha = 0.78f,
        outerHorizontalPadding = 40.dp
    ),
    lightBackground = AppBackgroundPalette(
        start = ObsidianLightColorTokens.backgroundGradientTop,
        accentOne = ObsidianLightColorTokens.backgroundGradientMiddle,
        accentTwo = ObsidianLightColorTokens.surfaceElevated,
        end = ObsidianLightColorTokens.backgroundGradientBottom
    ),
    darkBackground = AppBackgroundPalette(
        start = ObsidianDarkColorTokens.backgroundGradientTop,
        accentOne = ObsidianDarkColorTokens.backgroundGradientMiddle,
        accentTwo = ObsidianDarkColorTokens.surfaceElevated,
        end = ObsidianDarkColorTokens.backgroundGradientBottom
    ),
    lightColorTokens = ObsidianLightColorTokens,
    darkColorTokens = ObsidianDarkColorTokens,
    typographyTokens = ObsidianTypographyTokens,
)

val LocalAppThemePack = staticCompositionLocalOf { PaySmartThemePack }

internal fun appThemePackFor(variant: AppThemeVariant): AppThemePack {
    return when (variant) {
        AppThemeVariant.PAYSMART -> PaySmartThemePack
        AppThemeVariant.OBSIDIAN -> ObsidianThemePack
    }
}

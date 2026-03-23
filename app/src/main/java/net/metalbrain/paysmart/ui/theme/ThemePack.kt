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
    val darkBackground: AppBackgroundPalette
)

private val PaySmartLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = FreshGreen.copy(alpha = 0.24f),
    onPrimaryContainer = PaySmartTextPrimary,
    secondary = DeepBlueOcean,
    onSecondary = LightSteel,
    secondaryContainer = Color(0xFFD8ECEE),
    onSecondaryContainer = TranquilWaters,
    tertiary = FreshGreen,
    onTertiary = TranquilWaters,
    background = LightSteel,
    onBackground = PaySmartTextPrimary,
    surface = PaySmartSurface,
    onSurface = PaySmartTextPrimary,
    surfaceVariant = PaySmartSurfaceTint,
    onSurfaceVariant = PaySmartTextSecondary,
    outline = PaySmartOutline,
    error = PaySmartError,
    errorContainer = PaySmartErrorContainer,
    onError = Color.White,
    onErrorContainer = PaySmartOnErrorContainer
)

private val PaySmartDarkColorScheme = darkColorScheme(
    primary = FreshGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color(0xFFEFFAF4),
    secondary = Color(0xFF8ED5B1),
    onSecondary = Color(0xFF103128),
    secondaryContainer = Color(0xFF27443D),
    onSecondaryContainer = Color(0xFFDDF3E8),
    tertiary = Color(0xFF7FC4C8),
    onTertiary = Color(0xFF02282B),
    background = PaySmartDarkBackground,
    onBackground = PaySmartDarkOnSurface,
    surface = PaySmartDarkSurface,
    onSurface = PaySmartDarkOnSurface,
    surfaceVariant = PaySmartDarkSurfaceElevated,
    onSurfaceVariant = PaySmartDarkOnSurfaceVariant,
    outline = PaySmartDarkOutline,
    error = PaySmartError,
    errorContainer = Color(0xFF8C1D18),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val ObsidianLightColorScheme = lightColorScheme(
    primary = Color(0xFF5E5A76),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00CFFC),
    onPrimaryContainer = Color(0xFF00151B),
    secondary = Color(0xFF52606E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E4EF),
    onSecondaryContainer = Color(0xFF11202D),
    tertiary = Color(0xFF7B5A6C),
    onTertiary = Color.White,
    background = Color(0xFFF3F1F7),
    onBackground = Color(0xFF19171F),
    surface = Color(0xFFFCF8FF),
    onSurface = Color(0xFF1A1820),
    surfaceVariant = Color(0xFFE6E0EA),
    onSurfaceVariant = Color(0xFF4A4553),
    outline = Color(0xFF7B7584),
    error = PaySmartError,
    errorContainer = PaySmartErrorContainer,
    onError = Color.White,
    onErrorContainer = PaySmartOnErrorContainer
)

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = Color(0xFF69DAFF),
    onPrimary = Color(0xFF00202A),
    primaryContainer = Color(0xFF00CFFC),
    onPrimaryContainer = Color(0xFF00151B),
    secondary = Color(0xFF9FB2C7),
    onSecondary = Color(0xFF0F1720),
    secondaryContainer = Color(0xFF22303D),
    onSecondaryContainer = Color(0xFFE4EDF7),
    tertiary = Color(0xFF7CD7F4),
    onTertiary = Color(0xFF002733),
    background = Color(0xFF06090E),
    onBackground = Color(0xFFF1F3FC),
    surface = Color(0xFF0A0E14),
    onSurface = Color(0xFFF1F3FC),
    surfaceVariant = Color(0xFF1B232D),
    onSurfaceVariant = Color(0xFFB8C0CC),
    outline = Color(0xFF7E8B9B),
    error = PaySmartError,
    errorContainer = Color(0xFF8C1D18),
    onError = Color.White,
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
        start = PaySmartLightColorScheme.background,
        accentOne = FreshGreen.copy(alpha = 0.10f),
        accentTwo = DeepBlueOcean.copy(alpha = 0.06f),
        end = PaySmartLightColorScheme.background
    ),
    darkBackground = AppBackgroundPalette(
        start = PaySmartDarkColorScheme.background,
        accentOne = DeepBlueOcean.copy(alpha = 0.18f),
        accentTwo = PaySmartDarkColorScheme.surfaceVariant.copy(alpha = 0.45f),
        end = PaySmartDarkColorScheme.background
    )
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
        start = ObsidianLightColorScheme.background,
        accentOne = ObsidianLightColorScheme.primary.copy(alpha = 0.08f),
        accentTwo = ObsidianLightColorScheme.secondaryContainer.copy(alpha = 0.32f),
        end = ObsidianLightColorScheme.background
    ),
    darkBackground = AppBackgroundPalette(
        start = ObsidianDarkColorScheme.surface,
        accentOne = ObsidianDarkColorScheme.primary.copy(alpha = 0.14f),
        accentTwo = ObsidianDarkColorScheme.surfaceVariant.copy(alpha = 0.72f),
        end = ObsidianDarkColorScheme.background
    )
)

internal val LocalAppThemePack = staticCompositionLocalOf { PaySmartThemePack }

internal fun appThemePackFor(variant: AppThemeVariant): AppThemePack {
    return when (variant) {
        AppThemeVariant.PAYSMART -> PaySmartThemePack
        AppThemeVariant.OBSIDIAN -> ObsidianThemePack
    }
}

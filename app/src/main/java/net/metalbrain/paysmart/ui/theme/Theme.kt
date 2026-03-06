package net.metalbrain.paysmart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
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

@Composable
fun PaysmartTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


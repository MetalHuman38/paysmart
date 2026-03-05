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
    primary = PaySmartGreenLight,
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF123628),
    onPrimaryContainer = PaySmartGreenLight,
    secondary = PaySmartGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A242D),
    onSecondaryContainer = Color(0xFFE3EAF0),
    tertiary = PaySmartAccent,
    onTertiary = Color(0xFF00382A),
    background = PaySmartDarkBackground,
    onBackground = Color(0xFFF3F6FA),
    surface = PaySmartDarkSurface,
    onSurface = Color(0xFFF3F6FA),
    surfaceVariant = PaySmartDarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFC1CAD4),
    outline = Color(0xFF5B6773),
    error = PaySmartError,
    errorContainer = Color(0xFF8C1D18),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = PaySmartGreen,
    onPrimary = Color.White,
    primaryContainer = PaySmartSeaWhite,
    onPrimaryContainer = PaySmartTextPrimary,
    secondary = PaySmartGreenDark,
    onSecondary = Color.White,
    secondaryContainer = PaySmartSeaWhite,
    onSecondaryContainer = PaySmartTextPrimary,
    tertiary = PaySmartAccent,
    onTertiary = Color.White,
    background = PaySmartOffWhite,
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

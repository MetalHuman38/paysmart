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
    primaryContainer = PaySmartGreenDark,
    onPrimaryContainer = PaySmartGreenLight,
    secondary = PaySmartGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1D2A24),
    onSecondaryContainer = Color(0xFFDBE5E0),
    tertiary = PaySmartAccent,
    onTertiary = Color(0xFF00382A),
    background = PaySmartDarkBackground,
    onBackground = Color(0xFFE1E4DF),
    surface = PaySmartDarkSurface,
    onSurface = Color(0xFFE1E4DF),
    surfaceVariant = Color(0xFF1D2621),
    onSurfaceVariant = Color(0xFFBDC9C2),
    outline = Color(0xFF8B9A92),
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
    background = PaySmartSeaWhite,
    onBackground = PaySmartTextPrimary,
    surface = PaySmartSurface,
    onSurface = PaySmartTextPrimary,
    surfaceVariant = PaySmartSeaWhite,
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

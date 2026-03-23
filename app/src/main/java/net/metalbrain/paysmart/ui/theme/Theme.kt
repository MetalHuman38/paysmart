package net.metalbrain.paysmart.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.theme.data.AppThemeVariant

@SuppressLint("ObsoleteSdkInt")
@Composable
fun PaysmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeVariant: AppThemeVariant = AppThemeVariant.PAYSMART,
    content: @Composable () -> Unit
) {
    val themePack = appThemePackFor(themeVariant)
    val colorScheme = when {
        dynamicColor &&
            themePack.supportsDynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> themePack.darkColors
        else -> themePack.lightColors
    }

    CompositionLocalProvider(LocalAppThemePack provides themePack) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = themePack.typography,
            shapes = themePack.shapes,
            content = content
        )
    }
}

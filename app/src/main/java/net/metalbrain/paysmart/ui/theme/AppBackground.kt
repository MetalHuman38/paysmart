package net.metalbrain.paysmart.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance

@Composable
fun PaySmartAppBackground(
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val themePack = LocalAppThemePack.current
    val darkMode = colors.background.luminance() < 0.2f
    val palette = if (darkMode) themePack.darkBackground else themePack.lightBackground
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to palette.start,
                        0.40f to palette.accentOne,
                        0.72f to palette.accentTwo,
                        1f to palette.end
                    )
                )
            )
    ) {
        content()
    }
}

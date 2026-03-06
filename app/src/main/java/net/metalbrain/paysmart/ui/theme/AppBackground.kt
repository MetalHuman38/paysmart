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
    val darkMode = colors.background.luminance() < 0.2f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = if (darkMode) {
                        arrayOf(
                            0f to colors.background,
                            0.45f to DeepBlueOcean.copy(alpha = 0.18f),
                            0.75f to colors.surfaceVariant.copy(alpha = 0.45f),
                            1f to colors.background
                        )
                    } else {
                        arrayOf(
                            0f to colors.background,
                            0.40f to FreshGreen.copy(alpha = 0.10f),
                            0.70f to DeepBlueOcean.copy(alpha = 0.06f),
                            1f to colors.background
                        )
                    }
                )
            )
    ) {
        content()
    }
}

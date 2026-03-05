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
                            0.55f to colors.surfaceVariant.copy(alpha = 0.50f),
                            1f to colors.background
                        )
                    } else {
                        arrayOf(
                            0f to colors.background,
                            0.50f to colors.primaryContainer.copy(alpha = 0.24f),
                            1f to colors.background
                        )
                    }
                )
            )
    ) {
        content()
    }
}

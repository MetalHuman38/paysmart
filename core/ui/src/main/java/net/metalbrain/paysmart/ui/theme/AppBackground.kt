package net.metalbrain.paysmart.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun PaySmartAppBackground(
    content: @Composable () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to colors.backgroundGradientTop,
                        0.40f to colors.backgroundGradientMiddle,
                        0.72f to colors.surfaceElevated,
                        1f to colors.backgroundGradientBottom
                    )
                )
            )
    ) {
        content()
    }
}

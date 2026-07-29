package net.metalbrain.paysmart.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PaySmartAppBackground(
    content: @Composable () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
    ) {
        content()
    }
}

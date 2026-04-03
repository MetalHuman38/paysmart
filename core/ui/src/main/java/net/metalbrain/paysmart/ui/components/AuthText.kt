package net.metalbrain.paysmart.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun AuthScreenTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = PaysmartTheme.typographyTokens.heading2,
        color = PaysmartTheme.colorTokens.textPrimary
    )
}

@Composable
fun AuthScreenSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = PaysmartTheme.typographyTokens.bodyMedium,
        color = PaysmartTheme.colorTokens.textSecondary
    )
}

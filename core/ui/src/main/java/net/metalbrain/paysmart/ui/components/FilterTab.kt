package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun FilterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            colors.tabActiveBackground
        } else {
            colors.surfacePrimary
        },
        contentColor = if (selected) {
            colors.brandPrimary
        } else {
            colors.textSecondary
        },
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .heightIn(min = Dimens.minimumTouchTarget)
                .padding(horizontal = Dimens.md, vertical = Dimens.sm)
        )
    }
}

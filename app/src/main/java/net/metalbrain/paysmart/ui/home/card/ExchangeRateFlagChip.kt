package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.home.components.ExchangeRateFlagCircle
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ExchangeRateFlagChip(
    baseFlag: String,
    targetFlag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 72.dp)
                .padding(horizontal = Dimens.sm, vertical = Dimens.xs),
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExchangeRateFlagCircle(
                flag = baseFlag,
                modifier = Modifier.widthIn(min = 28.dp)
            )
            ExchangeRateFlagCircle(
                flag = targetFlag,
                modifier = Modifier.widthIn(min = 28.dp)
            )
        }
    }
}

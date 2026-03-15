package net.metalbrain.paysmart.core.features.transactions.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionFilterSheet(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onApply: (Set<String>) -> Unit
) {
    var localSelection by remember(selected, options) {
        mutableStateOf(selected.intersect(options.toSet()))
    }
    val shape = RoundedCornerShape(28.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.space8, vertical = Dimens.space4),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                )
                .padding(Dimens.space10),
            verticalArrangement = Arrangement.spacedBy(Dimens.space6)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            options.forEach { option ->
                TransactionFilterOptionRow(
                    label = option,
                    selected = option in localSelection,
                    onClick = {
                        localSelection = if (option in localSelection) {
                            localSelection - option
                        } else {
                            localSelection + option
                        }
                    }
                )
            }

            PrimaryButton(
                text = stringResource(R.string.sheet_filter_apply),
                onClick = { onApply(localSelection) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

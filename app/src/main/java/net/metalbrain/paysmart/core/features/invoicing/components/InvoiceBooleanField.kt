package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.booleanValue
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun InvoiceBooleanField(
    field: InvoiceField,
    label: String,
    supportingText: String?,
    onValueChange: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                supportingText?.let { copy ->
                    Text(
                        text = copy,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            androidx.compose.material3.Switch(
                checked = field.booleanValue() ?: false,
                onCheckedChange = onValueChange
            )
        }
    }
}

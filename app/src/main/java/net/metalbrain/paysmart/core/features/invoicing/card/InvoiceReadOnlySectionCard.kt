package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.features.invoicing.utils.fieldDisplayValue
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun InvoiceReadOnlySectionCard(
    section: InvoiceSection
) {
    InvoiceSurfaceCard {
        InvoiceSectionHeading(title = section.title)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            section.fields.forEach { field ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Dimens.sm))
                    Text(
                        text = fieldDisplayValue(field),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

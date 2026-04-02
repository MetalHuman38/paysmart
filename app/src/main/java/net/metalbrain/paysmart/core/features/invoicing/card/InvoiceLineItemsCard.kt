package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.invoicing.components.InvoiceDynamicField
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceCardTone
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun InvoiceLineItemsCard(
    invoice: Invoice,
    onLineItemFieldChanged: (index: Int, fieldKey: String, value: Any?) -> Unit
) {
    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = "Line items",
            body = "Capture each shift or task clearly so totals stay accurate."
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
            invoice.lineItems.forEachIndexed { index, lineItem ->
                InvoiceSurfaceCard(tone = InvoiceCardTone.Muted) {
                    Text(
                        text = "Line item ${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                        lineItem.fields.forEach { field ->
                            InvoiceDynamicField(
                                field = field,
                                readOnly = field.key == InvoiceFieldKeys.LINE_AMOUNT,
                                onValueChange = { onLineItemFieldChanged(index, field.key, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.core.features.invoicing.components.InvoiceDynamicField
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun InvoiceDynamicSectionCard(
    section: InvoiceSection,
    onFieldChanged: (fieldKey: String, value: Any?) -> Unit
) {
    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = section.title,
            body = if (section.fields.any(InvoiceField::required)) {
                "Required fields are marked and highlighted until completed."
            } else {
                null
            }
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            section.fields.forEach { field ->
                InvoiceDynamicField(
                    field = field,
                    readOnly = field.key == InvoiceFieldKeys.INVOICE_NUMBER,
                    onValueChange = { onFieldChanged(field.key, it) }
                )
            }
        }
    }
}

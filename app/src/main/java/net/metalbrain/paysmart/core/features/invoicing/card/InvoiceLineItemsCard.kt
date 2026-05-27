package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.components.InvoiceDynamicField
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceCardTone
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun InvoiceLineItemsCard(
    invoice: Invoice,
    onLineItemFieldChanged: (index: Int, fieldKey: String, value: Any?) -> Unit,
    onAddLineItem: () -> Unit,
    onRemoveLineItem: (String) -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = "Worked shifts",
            body = "Add only the shifts that belong on this invoice."
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
            invoice.lineItems.forEachIndexed { index, lineItem ->
                InvoiceSurfaceCard(tone = InvoiceCardTone.Muted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Shift ${index + 1}",
                            style = typography.labelLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { onRemoveLineItem(lineItem.id) },
                            enabled = invoice.lineItems.size > 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.invoice_shift_remove_action)
                            )
                        }
                    }
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

            OutlinedButton(
                onClick = onAddLineItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(text = stringResource(R.string.invoice_shift_add_action))
            }
        }
    }
}

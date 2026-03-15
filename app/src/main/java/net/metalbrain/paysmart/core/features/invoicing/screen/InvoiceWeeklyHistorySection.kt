package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceSummary

@Composable
internal fun InvoiceWeeklyHistorySection(
    invoices: List<InvoiceSummary>,
    isLoading: Boolean,
    onOpenInvoice: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.invoice_weekly_recent_finalized_title))
        when {
            isLoading -> {
                Text(text = stringResource(R.string.invoice_weekly_recent_finalized_loading))
            }

            invoices.isEmpty() -> {
                Text(text = stringResource(R.string.invoice_weekly_recent_finalized_empty))
            }

            else -> {
                invoices.forEach { invoice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onOpenInvoice(invoice.invoiceId) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.invoice_weekly_recent_finalized_item_title,
                                    invoice.invoiceNumber
                                )
                            )
                            Text(
                                text = stringResource(
                                    R.string.invoice_weekly_recent_finalized_item_subtitle,
                                    invoice.venueName,
                                    invoice.weekEndingDate
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceSummary
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun InvoiceWeeklyHistorySection(
    invoices: List<InvoiceSummary>,
    isLoading: Boolean,
    onOpenInvoice: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        InvoiceSectionHeading(title = stringResource(R.string.invoice_weekly_recent_finalized_title))

        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.invoice_weekly_recent_finalized_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            invoices.isEmpty() -> {
                Text(
                    text = stringResource(R.string.invoice_weekly_recent_finalized_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                invoices.forEach { invoice ->
                    InvoiceSurfaceCard(
                        tone = InvoiceCardTone.Muted,
                        onClick = { onOpenInvoice(invoice.invoiceId) }
                    ) {
                        Text(
                            text = stringResource(
                                R.string.invoice_weekly_recent_finalized_item_title,
                                invoice.invoiceNumber
                            ),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(
                                R.string.invoice_weekly_recent_finalized_item_subtitle,
                                invoice.venueName,
                                invoice.weekEndingDate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceCardTone
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.features.invoicing.utils.formatDecimal
import net.metalbrain.paysmart.core.features.invoicing.utils.formatMoneyMinor
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState


@Composable
fun InvoiceTotalsOverviewCard(
    state: InvoiceSetupUiState
) {
    InvoiceSurfaceCard(tone = InvoiceCardTone.Accent) {
        InvoiceSectionHeading(
            title = "Review & totals",
            body = "Check the invoice details before generating the final PDF."
        )
        Text(
            text = "Total hours: ${formatDecimal(state.draftInvoice.totals.totalHours)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Subtotal: ${formatMoneyMinor(state.draftInvoice.totals.subtotalMinor, state.draftInvoice.totals.currencyCode)}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Total: ${formatMoneyMinor(state.draftInvoice.totals.totalMinor, state.draftInvoice.totals.currencyCode)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (!state.canFinalize) {
            Text(
                text = "Complete invoice date, week ending, rate, and at least one worked shift before finalizing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
            )
        }
    }
}

package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceCardTone
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.features.invoicing.utils.formatDecimal
import net.metalbrain.paysmart.core.features.invoicing.utils.formatMoneyMinor
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun InvoiceTotalsOverviewCard(
    state: InvoiceSetupUiState
) {
    InvoiceSurfaceCard(tone = InvoiceCardTone.Accent) {
        val totals = state.draftInvoice.totals
        val colors = PaysmartTheme.colorTokens
        val typography = PaysmartTheme.typographyTokens

        InvoiceSectionHeading(
            title = "Totals",
            body = "Check the invoice details before generating the final PDF."
        )
        InvoiceTotalLine(
            label = "Hours",
            value = formatDecimal(totals.totalHours)
        )
        InvoiceTotalLine(
            label = "Subtotal",
            value = formatMoneyMinor(totals.subtotalMinor, totals.currencyCode)
        )
        InvoiceTotalLine(
            label = "Total",
            value = formatMoneyMinor(totals.totalMinor, totals.currencyCode),
            emphasized = true
        )
        if (!state.canFinalize) {
            Text(
                text = "Add missing details to continue.",
                style = typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun InvoiceTotalLine(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = typography.bodyMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            style = if (emphasized) typography.heading4 else typography.bodyMedium,
            color = colors.textPrimary,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.LocalDate

@Composable
internal fun InvoiceWeeklyMetaFields(
    state: InvoiceSetupUiState,
    onInvoiceDateChanged: (String) -> Unit,
    onWeekEndingDateChanged: (String) -> Unit,
    onHourlyRateChanged: (String) -> Unit
) {
    val invoiceFallbackDate = runCatching { LocalDate.parse(state.weeklyDraft.invoiceDate) }
        .getOrDefault(LocalDate.now())
    val weekEndingFallbackDate = runCatching { LocalDate.parse(state.weeklyDraft.weekEndingDate) }
        .getOrDefault(invoiceFallbackDate)

    InvoiceSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            InvoiceDateField(
                value = state.weeklyDraft.invoiceDate,
                onDateSelected = onInvoiceDateChanged,
                label = stringResource(R.string.invoice_weekly_invoice_date_label),
                fallbackDate = invoiceFallbackDate
            )
            InvoiceDateField(
                value = state.weeklyDraft.weekEndingDate,
                onDateSelected = onWeekEndingDateChanged,
                label = stringResource(R.string.invoice_weekly_week_ending_label),
                fallbackDate = weekEndingFallbackDate
            )
            InvoiceInputField(
                value = state.weeklyDraft.hourlyRateInput,
                onValueChange = onHourlyRateChanged,
                label = stringResource(R.string.invoice_weekly_hourly_rate_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

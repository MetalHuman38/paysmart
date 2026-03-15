package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
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

    fun dp(value: Int) = value.dp
    
    Column(verticalArrangement = Arrangement.spacedBy(dp(8))) {
        InvoiceDateField(
            value = state.weeklyDraft.invoiceDate,
            onDateSelected = onInvoiceDateChanged,
            label = stringResource(R.string.invoice_weekly_invoice_date_label),
            modifier = Modifier.fillMaxWidth(),
            fallbackDate = invoiceFallbackDate
        )
        InvoiceDateField(
            value = state.weeklyDraft.weekEndingDate,
            onDateSelected = onWeekEndingDateChanged,
            label = stringResource(R.string.invoice_weekly_week_ending_label),
            modifier = Modifier.fillMaxWidth(),
            fallbackDate = weekEndingFallbackDate
        )
        OutlinedTextField(
            value = state.weeklyDraft.hourlyRateInput,
            onValueChange = onHourlyRateChanged,
            label = { Text(stringResource(R.string.invoice_weekly_hourly_rate_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

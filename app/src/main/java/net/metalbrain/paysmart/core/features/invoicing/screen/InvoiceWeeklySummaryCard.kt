package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import java.util.Locale

/**
 * A composable that displays a summary card for a weekly invoice.
 *
 * It calculates and displays the total hours worked and the resulting subtotal
 * based on the provided hourly rate.
 *
 * @param totalHours The total number of hours worked during the week.
 * @param hourlyRateInput A string representation of the hourly rate to be applied to the hours.
 * If the string is not a valid number, a rate of 0.0 is used.
 */
@Composable
fun InvoiceWeeklySummaryCard(
    totalHours: Double,
    hourlyRateInput: String
) {
    val hourlyRate = hourlyRateInput.toDoubleOrNull() ?: 0.0
    val subtotal = totalHours * hourlyRate

    InvoiceSurfaceCard(tone = InvoiceCardTone.Accent) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            Text(
                text = stringResource(
                    id = R.string.invoice_weekly_total_hours_value,
                    String.format(Locale.US, "%.2f", totalHours)
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(INVOICE_TOTAL_HOURS_TAG)
            )
            Text(
                text = stringResource(
                    id = R.string.invoice_weekly_subtotal_value,
                    String.format(Locale.US, "%.2f", subtotal)
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag(INVOICE_SUBTOTAL_TAG)
            )
        }
    }
}

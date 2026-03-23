package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceShiftDraft
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.LocalDate

/**
 * A composable that renders a list of input rows for weekly shifts within an invoice.
 * Each row is displayed inside a shared invoice surface and includes fields for editing the shift
 * date and hours.
 *
 * @param rows The list of [InvoiceShiftDraft] objects representing the shifts to be displayed.
 * @param onShiftDateChanged A callback triggered when the date text field of a shift is updated.
 * Provides the index of the row and the new string value.
 * @param onShiftHoursChanged A callback triggered when the hours text field of a shift is updated.
 * Provides the index of the row and the new string value.
 */
@Composable
fun InvoiceWeeklyShiftRows(
    rows: List<InvoiceShiftDraft>,
    onShiftDateChanged: (index: Int, value: String) -> Unit,
    onShiftHoursChanged: (index: Int, value: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        rows.forEachIndexed { index, row ->
            InvoiceSurfaceCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Text(
                        text = row.dayLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        InvoiceDateField(
                            value = row.workDate,
                            onDateSelected = { onShiftDateChanged(index, it) },
                            label = stringResource(R.string.invoice_weekly_shift_date_label),
                            modifier = Modifier.weight(1f),
                            testTag = invoiceDateFieldTag(index),
                            fallbackDate = LocalDate.now()
                        )
                        InvoiceInputField(
                            value = row.hoursInput,
                            onValueChange = { onShiftHoursChanged(index, it) },
                            label = stringResource(R.string.invoice_weekly_shift_hours_label),
                            modifier = Modifier.weight(0.55f),
                            testTag = invoiceHoursFieldTag(index),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }
    }
}

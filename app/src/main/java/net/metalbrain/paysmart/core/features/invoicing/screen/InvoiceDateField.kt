package net.metalbrain.paysmart.core.features.invoicing.screen

import android.app.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.R
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun InvoiceDateField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    fallbackDate: LocalDate = LocalDate.now()
) {
    val context = LocalContext.current
    val initialDate = rememberDateOrFallback(value, fallbackDate)

    fun showDatePicker() {
        DatePickerDialog(
            context,
            R.style.SpinnerDatePickerDialog,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth).toString())
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).show()
    }

    InvoiceInputField(
        value = value,
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        label = label,
        testTag = testTag,
        trailingIcon = {
            IconButton(onClick = ::showDatePicker) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = label
                )
            }
        }
    )
}

private fun rememberDateOrFallback(raw: String, fallbackDate: LocalDate): LocalDate {
    return try {
        LocalDate.parse(raw.trim())
    } catch (_: DateTimeParseException) {
        fallbackDate
    }
}

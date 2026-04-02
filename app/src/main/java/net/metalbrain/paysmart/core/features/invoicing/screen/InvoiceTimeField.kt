package net.metalbrain.paysmart.core.features.invoicing.screen

import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
internal fun InvoiceTimeField(
    value: String,
    label: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    fallbackTime: LocalTime = LocalTime.of(9, 0)
) {
    val context = LocalContext.current
    val initialTime = rememberTimeOrFallback(value, fallbackTime)

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(
                    LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
                )
            },
            initialTime.hour,
            initialTime.minute,
            true
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
            IconButton(onClick = ::showTimePicker) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = label
                )
            }
        }
    )
}

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun rememberTimeOrFallback(raw: String, fallbackTime: LocalTime): LocalTime {
    return try {
        LocalTime.parse(raw.trim(), TIME_FORMATTER)
    } catch (_: DateTimeParseException) {
        fallbackTime
    }
}

package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InvoiceDateField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    fallbackDate: LocalDate = LocalDate.now(),
    earliestDate: LocalDate = LocalDate.of(2000, 1, 1),
    latestDate: LocalDate = LocalDate.now().plusYears(10)
) {
    val initialDate = rememberDateOrFallback(value, fallbackDate)
    var showDatePicker by remember { mutableStateOf(false) }

    InvoiceInputField(
        value = value,
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        label = label,
        testTag = testTag,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = label
                )
            }
        }
    )

    if (showDatePicker) {
        InvoiceDatePickerDialog(
            title = label,
            initialDate = initialDate,
            earliestDate = earliestDate,
            latestDate = latestDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate.toString())
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDatePickerDialog(
    title: String,
    initialDate: LocalDate,
    earliestDate: LocalDate,
    latestDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val selectableDates = remember(earliestDate, latestDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcMillisToLocalDate(utcTimeMillis)
                return !date.isBefore(earliestDate) && !date.isAfter(latestDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in earliestDate.year..latestDate.year
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcMillis(),
        yearRange = earliestDate.year..latestDate.year,
        selectableDates = selectableDates
    )
    val selectedDate = datePickerState.selectedDateMillis
        ?.let(::utcMillisToLocalDate)
        ?: initialDate

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f)
                                )
                            )
                        )
                        .padding(horizontal = Dimens.lg, vertical = Dimens.lg)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        )
                        Text(
                            text = selectedDate.format(DIALOG_DATE_FORMATTER),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 500.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.primary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        navigationContentColor = MaterialTheme.colorScheme.onSurface,
                        dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.common_cancel))
                    }
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                onDateSelected(utcMillisToLocalDate(millis))
                            }
                        },
                        enabled = datePickerState.selectedDateMillis != null,
                        modifier = Modifier.widthIn(min = 72.dp)
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

private val DIALOG_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

private fun rememberDateOrFallback(raw: String, fallbackDate: LocalDate): LocalDate {
    return try {
        LocalDate.parse(raw.trim())
    } catch (_: DateTimeParseException) {
        fallbackDate
    }
}

private fun LocalDate.toUtcMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun utcMillisToLocalDate(utcTimeMillis: Long): LocalDate {
    return Instant.ofEpochMilli(utcTimeMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}

package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceDateField
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceInputField
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceTimeField
import net.metalbrain.paysmart.core.features.invoicing.utils.fieldDisplayValue
import net.metalbrain.paysmart.core.features.invoicing.utils.isCompletedForDisplay
import net.metalbrain.paysmart.core.features.invoicing.utils.prefersMultiLine
import net.metalbrain.paysmart.core.invoice.model.FieldType
import net.metalbrain.paysmart.core.invoice.model.InvoiceField


@Composable
fun InvoiceDynamicField(
    field: InvoiceField,
    readOnly: Boolean = false,
    onValueChange: (Any?) -> Unit
) {
    val label = if (field.required) "${field.label} *" else field.label
    val showRequiredError = field.required && !field.isCompletedForDisplay()
    val supportingText = if (showRequiredError) "Required" else null

    when (field.type) {
        FieldType.TEXT -> {
            InvoiceInputField(
                value = fieldDisplayValue(field),
                onValueChange = onValueChange,
                label = label,
                readOnly = readOnly,
                singleLine = !field.prefersMultiLine(),
                placeholder = field.placeholder,
                supportingText = supportingText,
                isError = showRequiredError
            )
        }

        FieldType.NUMBER,
        FieldType.CURRENCY,
        FieldType.DURATION -> {
            InvoiceInputField(
                value = fieldDisplayValue(field),
                onValueChange = onValueChange,
                label = label,
                readOnly = readOnly,
                placeholder = field.placeholder,
                supportingText = supportingText,
                isError = showRequiredError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
        }

        FieldType.DATE -> {
            InvoiceDateField(
                value = fieldDisplayValue(field),
                label = label,
                onDateSelected = { onValueChange(it) }
            )
        }

        FieldType.TIME -> {
            InvoiceTimeField(
                value = fieldDisplayValue(field),
                label = label,
                onTimeSelected = { onValueChange(it) }
            )
        }

        FieldType.DROPDOWN -> {
            InvoiceDropdownField(
                field = field,
                label = label,
                supportingText = supportingText,
                showRequiredError = showRequiredError,
                onValueChange = { onValueChange(it) }
            )
        }

        FieldType.BOOLEAN -> {
            InvoiceBooleanField(
                field = field,
                label = label,
                supportingText = supportingText,
                onValueChange = { onValueChange(it) }
            )
        }
    }
}

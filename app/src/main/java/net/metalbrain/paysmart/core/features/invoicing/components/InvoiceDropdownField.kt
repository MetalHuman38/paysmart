package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.core.features.invoicing.utils.fieldDisplayValue
import net.metalbrain.paysmart.core.invoice.model.InvoiceField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDropdownField(
    field: InvoiceField,
    label: String,
    supportingText: String?,
    showRequiredError: Boolean,
    onValueChange: (String) -> Unit
) {
    var expanded by rememberSaveable(field.key) {
        androidx.compose.runtime.mutableStateOf(false)
    }
    val placeholderText = field.placeholder
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            value = fieldDisplayValue(field),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = if (placeholderText == null) null else {
                { Text(placeholderText) }
            },
            supportingText = if (supportingText == null) null else {
                { Text(supportingText) }
            },
            isError = showRequiredError,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = MaterialTheme.shapes.medium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            field.options.orEmpty().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    }
                )
            }
        }
    }
}

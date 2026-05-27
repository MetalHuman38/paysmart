package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSectionHeading
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.features.invoicing.utils.fieldDisplayValue
import net.metalbrain.paysmart.core.features.invoicing.utils.prefersMultiLine
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun InvoiceReadOnlySectionCard(
    section: InvoiceSection
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    InvoiceSurfaceCard {
        InvoiceSectionHeading(title = section.title)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            section.fields.forEach { field ->
                InvoiceReadOnlyFieldRow(
                    field = field,
                    labelColor = colors.textSecondary,
                    valueColor = colors.textPrimary,
                    labelStyle = typography.bodySmall,
                    valueStyle = typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun InvoiceReadOnlyFieldRow(
    field: InvoiceField,
    labelColor: Color,
    valueColor: Color,
    labelStyle: TextStyle,
    valueStyle: TextStyle
) {
    val value = fieldDisplayValue(field)
    val multiline = field.prefersMultiLine() || value.length > 32
    if (multiline) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.space2)
        ) {
            Text(
                text = field.label,
                style = labelStyle,
                color = labelColor
            )
            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                style = valueStyle,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Text(
                text = field.label,
                modifier = Modifier.weight(1f),
                style = labelStyle,
                color = labelColor
            )
            Text(
                text = value,
                modifier = Modifier.weight(1.4f),
                style = valueStyle,
                color = valueColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

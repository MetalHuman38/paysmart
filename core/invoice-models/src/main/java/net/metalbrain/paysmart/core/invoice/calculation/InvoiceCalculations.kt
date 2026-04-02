package net.metalbrain.paysmart.core.invoice.calculation

import kotlin.math.roundToLong
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.LineItem
import net.metalbrain.paysmart.core.invoice.model.doubleValue
import net.metalbrain.paysmart.core.invoice.model.findField

object InvoiceCalculations {

    fun calculateLineAmountMinor(lineItem: LineItem): Long {
        val hours = lineItem.findField(InvoiceFieldKeys.LINE_HOURS).asDecimalOrZero()
        val rate = lineItem.findField(InvoiceFieldKeys.LINE_RATE).asDecimalOrZero()
        val bonus = lineItem.findField(InvoiceFieldKeys.LINE_BONUS).asDecimalOrZero()
        return ((hours * rate + bonus) * 100.0).roundToLong()
    }

    fun calculateLineHours(lineItem: LineItem): Double {
        return lineItem.findField(InvoiceFieldKeys.LINE_HOURS).asDecimalOrZero()
    }

    fun recalculate(invoice: Invoice): Invoice {
        val subtotalMinor = invoice.lineItems.sumOf(::calculateLineAmountMinor)
        val totalHours = invoice.lineItems.sumOf(::calculateLineHours)
        return invoice.copy(
            totals = invoice.totals.copy(
                totalHours = totalHours,
                subtotalMinor = subtotalMinor,
                totalMinor = subtotalMinor
            )
        )
    }

    fun withComputedAmounts(lineItems: List<LineItem>): List<LineItem> {
        return lineItems.map { lineItem ->
            val computedAmountMinor = calculateLineAmountMinor(lineItem)
            val updatedFields = lineItem.fields.map { field ->
                if (field.key == InvoiceFieldKeys.LINE_AMOUNT) {
                    field.copy(value = computedAmountMinor / 100.0)
                } else {
                    field
                }
            }
            lineItem.copy(fields = updatedFields)
        }
    }

    private fun InvoiceField?.asDecimalOrZero(): Double {
        return this?.doubleValue() ?: 0.0
    }
}

package net.metalbrain.paysmart.core.invoice.factory

import java.util.UUID
import net.metalbrain.paysmart.core.invoice.calculation.InvoiceCalculations
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.LineItem
import net.metalbrain.paysmart.core.invoice.model.Totals
import net.metalbrain.paysmart.core.invoice.template.InvoiceTemplateCatalog

object InvoiceFactory {

    fun createFromTemplate(
        templateId: String,
        invoiceId: String = UUID.randomUUID().toString(),
        professionId: String? = null,
        initialLineItemCount: Int = 1,
        currencyCode: String = "GBP"
    ): Invoice {
        val template = requireNotNull(InvoiceTemplateCatalog.template(templateId)) {
            "Unknown invoice template: $templateId"
        }
        val lineItems = List(initialLineItemCount.coerceAtLeast(1)) { index ->
            LineItem(
                id = "line_item_$index",
                fields = template.lineItemFields.map { field -> field.copy() }
            )
        }
        return InvoiceCalculations.recalculate(
            Invoice(
                id = invoiceId,
                templateId = template.id,
                professionId = professionId ?: template.professionId,
                sections = template.sections.sortedBy { it.order },
                lineItems = InvoiceCalculations.withComputedAmounts(lineItems),
                totals = Totals(currencyCode = currencyCode)
            )
        )
    }
}

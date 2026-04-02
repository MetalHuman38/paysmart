package net.metalbrain.paysmart.core.features.invoicing.utils

import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep

fun stepBody(step: InvoiceFormStep): String {
    return when (step) {
        InvoiceFormStep.QUICK_START -> "Choose the closest fit so PaySmart can start with the right invoice structure."
        InvoiceFormStep.INVOICE_INFO -> "Set the key invoice dates first so the rest of the form stays anchored."
        InvoiceFormStep.WORKER_DETAILS -> "These details appear on the PDF and save you time on the next invoice."
        InvoiceFormStep.CLIENT_DETAILS -> "Choose a saved venue or add a new client location before filling the work."
        InvoiceFormStep.WORK_DETAILS -> "Capture each shift clearly. Totals update automatically as you type."
        InvoiceFormStep.REVIEW -> "Review everything in one place before you finalize and generate the invoice."
    }
}

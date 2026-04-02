package net.metalbrain.paysmart.core.features.invoicing.utils

import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep

fun stepTitle(step: InvoiceFormStep): String {
    return when (step) {
        InvoiceFormStep.QUICK_START -> "Pick what best describes your work"
        InvoiceFormStep.INVOICE_INFO -> "Invoice info"
        InvoiceFormStep.WORKER_DETAILS -> "Worker details"
        InvoiceFormStep.CLIENT_DETAILS -> "Client / venue details"
        InvoiceFormStep.WORK_DETAILS -> "Work details"
        InvoiceFormStep.REVIEW -> "Review & totals"
    }
}

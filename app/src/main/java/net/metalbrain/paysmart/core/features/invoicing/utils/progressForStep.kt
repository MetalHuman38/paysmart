package net.metalbrain.paysmart.core.features.invoicing.utils

import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep

val PROGRESS_VISIBLE_STEPS = listOf(
    InvoiceFormStep.INVOICE_INFO,
    InvoiceFormStep.WORKER_DETAILS,
    InvoiceFormStep.CLIENT_DETAILS,
    InvoiceFormStep.WORK_DETAILS,
    InvoiceFormStep.REVIEW
)

fun progressiveStepIndex(step: InvoiceFormStep): Int {
    return PROGRESS_VISIBLE_STEPS.indexOf(step).coerceAtLeast(0)
}

fun progressForStep(step: InvoiceFormStep): Float {
    if (step == InvoiceFormStep.QUICK_START) return 0.12f
    val index = progressiveStepIndex(step) + 1
    return index.toFloat() / PROGRESS_VISIBLE_STEPS.size.toFloat()
}

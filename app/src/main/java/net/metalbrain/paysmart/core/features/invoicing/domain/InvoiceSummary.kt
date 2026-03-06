package net.metalbrain.paysmart.core.features.invoicing.domain

data class InvoiceSummary(
    val invoiceId: String,
    val invoiceNumber: String,
    val status: String,
    val subtotalMinor: Int,
    val currency: String,
    val venueName: String,
    val weekEndingDate: String,
    val createdAtMs: Long
)

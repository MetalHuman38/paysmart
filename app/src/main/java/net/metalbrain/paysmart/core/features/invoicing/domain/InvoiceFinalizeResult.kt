package net.metalbrain.paysmart.core.features.invoicing.domain

data class InvoiceFinalizeResult(
    val invoiceId: String,
    val invoiceNumber: String,
    val status: String,
    val sequenceNumber: Int,
    val totalHours: Double,
    val hourlyRate: Double,
    val subtotalMinor: Int,
    val currency: String,
    val venueName: String,
    val weekEndingDate: String,
    val createdAtMs: Long
)

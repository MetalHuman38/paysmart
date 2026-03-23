package net.metalbrain.paysmart.domain.model

data class TransactionStatusUpdate(
    val status: String,
    val timestampMs: Long
)

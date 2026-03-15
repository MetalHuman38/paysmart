package net.metalbrain.paysmart.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val iconRes: Int,
    val createdAtMs: Long,
    val updatedAtMs: Long = createdAtMs,
    val provider: String? = null,
    val paymentRail: String? = null,
    val reference: String = id,
    val externalReference: String? = null,
    val statusTimeline: List<TransactionStatusUpdate> = emptyList()
) {
    val date: String
        get() = Instant.ofEpochMilli(createdAtMs)
            .atZone(ZoneId.systemDefault())
            .format(TRANSACTION_DATE_FORMAT)

    val time: String
        get() = Instant.ofEpochMilli(createdAtMs)
            .atZone(ZoneId.systemDefault())
            .format(TRANSACTION_TIME_FORMAT)
}

private val TRANSACTION_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

private val TRANSACTION_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.US)

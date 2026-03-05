package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.LinkedHashMap
import java.util.Locale
import javax.inject.Inject

class FakeTransactionRepository @Inject constructor() : TransactionRepository {
    private val staticTransactions: List<Transaction> = emptyList()

    private val simulatedTopUps = LinkedHashMap<String, SimulatedTopUp>()
    private val transactions = MutableStateFlow(sortNewestFirst(staticTransactions))

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactions.map { rows -> sortNewestFirst(rows) }

    override suspend fun getTransactions(): List<Transaction> {
        delay(120)
        return sortNewestFirst(transactions.value)
    }

    override suspend fun upsertAddMoneySimulation(
        sessionId: String,
        amountMinor: Int,
        currency: String,
        status: String,
        createdAtMs: Long
    ) {
        val normalizedSessionId = sessionId.trim()
        if (normalizedSessionId.isBlank()) return

        val rowId = "stripe_$normalizedSessionId"
        val previous = simulatedTopUps[rowId]
        val persistedCreatedAt = previous?.createdAtMs ?: createdAtMs

        simulatedTopUps[rowId] = SimulatedTopUp(
            id = rowId,
            sessionId = normalizedSessionId,
            amountMinor = amountMinor,
            currency = currency.trim().uppercase(Locale.US).ifBlank { "GBP" },
            status = mapAddMoneyStatusToTransactionStatus(status),
            createdAtMs = persistedCreatedAt
        )

        val dynamicRows = simulatedTopUps.values
            .sortedByDescending { it.createdAtMs }
            .map { it.toTransaction() }
        transactions.value = sortNewestFirst(dynamicRows)
    }
}

private data class SimulatedTopUp(
    val id: String,
    val sessionId: String,
    val amountMinor: Int,
    val currency: String,
    val status: String,
    val createdAtMs: Long
)

private fun SimulatedTopUp.toTransaction(): Transaction {
    val createdAt = Instant.ofEpochMilli(createdAtMs)
        .atZone(ZoneId.systemDefault())
    val amount = amountMinor.toDouble() / 100.0

    return Transaction(
        id = id,
        title = "Top up via Stripe test",
        time = createdAt.format(TIME_FORMAT),
        amount = amount,
        currency = currency,
        status = status,
        date = createdAt.format(DATE_FORMAT),
        iconRes = R.drawable.ic_topup_bank
    )
}

private fun mapAddMoneyStatusToTransactionStatus(status: String): String {
    return when (status.trim().lowercase(Locale.US)) {
        "succeeded" -> "Successful"
        "failed" -> "Failed"
        "expired", "canceled", "cancelled" -> "Cancelled"
        "pending", "created" -> "In Progress"
        else -> "In Progress"
    }
}

private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

private val TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.US)

private val SORT_DATE_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.US)

private fun sortNewestFirst(items: List<Transaction>): List<Transaction> {
    return items.sortedByDescending { transaction ->
        parseSortEpochMillis(transaction)
    }
}

private fun parseSortEpochMillis(transaction: Transaction): Long {
    val rawDateTime = "${transaction.date.trim()} ${transaction.time.trim()}"
    return runCatching {
        LocalDateTime.parse(rawDateTime, SORT_DATE_TIME_FORMAT)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrDefault(0L)
}

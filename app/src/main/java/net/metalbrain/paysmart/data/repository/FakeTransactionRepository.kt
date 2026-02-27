package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.LinkedHashMap
import java.util.Locale
import javax.inject.Inject

class FakeTransactionRepository @Inject constructor() : TransactionRepository {
    private val staticTransactions = listOf(
        Transaction(
            id = "seed_1",
            title = "To Kalejaiye Aderonke Ade...",
            time = "18:58",
            amount = -20.0,
            currency = "GBP",
            status = "Successful",
            date = "9 Jan 2026",
            iconRes = R.drawable.ic_send
        ),
        Transaction(
            id = "seed_2",
            title = "Topup via MONZO BANK LIMI...",
            time = "18:56",
            amount = 20.0,
            currency = "GBP",
            status = "Successful",
            date = "9 Jan 2026",
            iconRes = R.drawable.ic_topup_mastercard
        ),
        Transaction(
            id = "seed_3",
            title = "Topup via MONZO BANK LIMI...",
            time = "17:47",
            amount = 20.0,
            currency = "GBP",
            status = "Successful",
            date = "14 Nov 2025",
            iconRes = R.drawable.ic_topup_bank
        ),
        Transaction(
            id = "seed_4",
            title = "To Kalejaiye Aderonke Ade...",
            time = "17:49",
            amount = -20.0,
            currency = "GBP",
            status = "Cancelled",
            date = "14 Nov 2025",
            iconRes = R.drawable.ic_send
        ),
        Transaction(
            id = "seed_5",
            title = "To Wale Kalejaiye",
            time = "13:55",
            amount = -10.0,
            currency = "GBP",
            status = "Failed",
            date = "24 Oct 2025",
            iconRes = R.drawable.ic_send
        )
    )

    private val simulatedTopUps = LinkedHashMap<String, SimulatedTopUp>()
    private val transactions = MutableStateFlow(staticTransactions)

    override fun observeTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun getTransactions(): List<Transaction> {
        delay(120)
        return transactions.value
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
        transactions.value = dynamicRows + staticTransactions
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

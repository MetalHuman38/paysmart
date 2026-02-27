package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.Transaction

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    suspend fun getTransactions(): List<Transaction>

    suspend fun upsertAddMoneySimulation(
        sessionId: String,
        amountMinor: Int,
        currency: String,
        status: String,
        createdAtMs: Long = System.currentTimeMillis()
    )
}

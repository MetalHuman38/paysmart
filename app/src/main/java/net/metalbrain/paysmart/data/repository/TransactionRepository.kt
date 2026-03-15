package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.domain.model.Transaction

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    suspend fun getTransactions(): List<Transaction>

    suspend fun recordAddMoneySession(
        session: AddMoneySessionData,
        recordedAtMs: Long = System.currentTimeMillis()
    )
}

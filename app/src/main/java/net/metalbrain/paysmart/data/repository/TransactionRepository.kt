package net.metalbrain.paysmart.data.repository

import net.metalbrain.paysmart.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(): List<Transaction>
}

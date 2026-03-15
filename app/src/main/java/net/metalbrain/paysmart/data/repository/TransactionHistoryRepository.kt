package net.metalbrain.paysmart.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.TransactionHistoryQuery

interface TransactionHistoryRepository {
    fun pagedTransactions(
        query: TransactionHistoryQuery = TransactionHistoryQuery(),
        pageSize: Int = 30
    ): Flow<PagingData<Transaction>>

    fun observeTransaction(transactionId: String): Flow<Transaction?>

    fun observeAvailableStatuses(): Flow<List<String>>

    fun observeAvailableCurrencies(): Flow<List<String>>
}

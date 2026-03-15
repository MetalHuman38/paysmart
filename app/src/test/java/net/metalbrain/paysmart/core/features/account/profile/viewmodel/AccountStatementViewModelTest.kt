package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.data.repository.TransactionHistoryRepository
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.TransactionHistoryQuery
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountStatementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `transactions requests default page size of 30`() = runTest {
        val repository = FakeTransactionHistoryRepository()
        val viewModel = AccountStatementViewModel(repository)

        viewModel.transactions.first()

        assertEquals(30, repository.lastRequestedPageSize)
        assertEquals(TransactionHistoryQuery(), repository.lastRequestedQuery)
    }
}

private class FakeTransactionHistoryRepository : TransactionHistoryRepository {
    var lastRequestedPageSize: Int? = null
    var lastRequestedQuery: TransactionHistoryQuery? = null

    override fun pagedTransactions(
        query: TransactionHistoryQuery,
        pageSize: Int
    ): Flow<PagingData<Transaction>> {
        lastRequestedQuery = query
        lastRequestedPageSize = pageSize
        return flowOf(PagingData.empty())
    }

    override fun observeTransaction(transactionId: String): Flow<Transaction?> = flowOf(null)

    override fun observeAvailableStatuses(): Flow<List<String>> = flowOf(emptyList())

    override fun observeAvailableCurrencies(): Flow<List<String>> = flowOf(emptyList())
}

package net.metalbrain.paysmart.core.features.transactions.viewmodel

import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.data.repository.TransactionHistoryRepository
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.TransactionHistoryQuery
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load observes transaction directly by id`() = runTest {
        val transaction = Transaction(
            id = "tx_123",
            title = "Top up via Stripe",
            amount = 25.0,
            currency = "GBP",
            status = "Successful",
            iconRes = 0,
            createdAtMs = 1_700_000_000_000
        )
        val repository = FakeTransactionHistoryRepositoryForDetail(
            transactionsById = mapOf("tx_123" to transaction)
        )
        val viewModel = TransactionDetailViewModel(repository)
        val collector = backgroundScope.launch {
            viewModel.uiState.collectLatest { }
        }

        viewModel.load("tx_123")
        advanceUntilIdle()

        assertEquals(listOf("tx_123"), repository.requestedIds)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("tx_123", viewModel.uiState.value.transaction?.id)
        collector.cancel()
    }
}

private class FakeTransactionHistoryRepositoryForDetail(
    transactionsById: Map<String, Transaction>
) : TransactionHistoryRepository {
    private val transactions = transactionsById
    val requestedIds = mutableListOf<String>()

    override fun pagedTransactions(
        query: TransactionHistoryQuery,
        pageSize: Int
    ): Flow<PagingData<Transaction>> = flowOf(PagingData.empty())

    override fun observeTransaction(transactionId: String): Flow<Transaction?> {
        requestedIds += transactionId
        return MutableStateFlow(transactions[transactionId])
    }

    override fun observeAvailableStatuses(): Flow<List<String>> = flowOf(emptyList())

    override fun observeAvailableCurrencies(): Flow<List<String>> = flowOf(emptyList())
}

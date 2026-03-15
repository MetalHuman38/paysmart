package net.metalbrain.paysmart.core.features.transactions.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [TransactionsViewModel].
 *
 * This class verifies the business logic for:
 * - Transaction sorting (ensuring newest transactions appear first).
 * - Filtering transactions by status and currency.
 * - Dynamic updates of available filter values based on the repository data.
 * - Correct combination and clearing of multiple filter criteria.
 *
 * It uses [MainDispatcherRule] to manage coroutine execution and a fake repository
 * to simulate transaction data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `filteredTransactions keeps newest transaction first`() = runTest {
        val repository = FakeTransactionRepositoryForTest(
            initial = listOf(
                transaction(
                    id = "older",
                    createdAtMs = 100L,
                    status = "Successful"
                ),
                transaction(
                    id = "newer",
                    createdAtMs = 200L,
                    status = "In Progress"
                )
            )
        )

        val viewModel = TransactionsViewModel(repository)
        val collector = backgroundScope.launch {
            viewModel.filteredTransactions.collectLatest { }
        }
        advanceUntilIdle()

        assertEquals(listOf("newer", "older"), viewModel.filteredTransactions.value.map { it.id })
        collector.cancel()
    }

    @Test
    fun `status filter keeps newest-first ordering`() = runTest {
        val repository = FakeTransactionRepositoryForTest(
            initial = listOf(
                transaction(
                    id = "success_old",
                    createdAtMs = 100L,
                    status = "Successful"
                ),
                transaction(
                    id = "success_new",
                    createdAtMs = 300L,
                    status = "Successful"
                ),
                transaction(
                    id = "failed_new",
                    createdAtMs = 400L,
                    status = "Failed"
                )
            )
        )

        val viewModel = TransactionsViewModel(repository)
        val collector = backgroundScope.launch {
            viewModel.filteredTransactions.collectLatest { }
        }
        viewModel.setStatusFilter(setOf("Successful"))
        advanceUntilIdle()

        assertEquals(
            listOf("success_new", "success_old"),
            viewModel.filteredTransactions.value.map { it.id }
        )
        collector.cancel()
    }

    @Test
    fun `filters combine and clear without breaking available filter lists`() = runTest {
        val repository = FakeTransactionRepositoryForTest(
            initial = listOf(
                transaction(
                    id = "gbp_success",
                    createdAtMs = 300L,
                    status = "Successful",
                    currency = "GBP"
                ),
                transaction(
                    id = "usd_success",
                    createdAtMs = 200L,
                    status = "Successful",
                    currency = "USD"
                ),
                transaction(
                    id = "gbp_failed",
                    createdAtMs = 100L,
                    status = "Failed",
                    currency = "GBP"
                )
            )
        )

        val viewModel = TransactionsViewModel(repository)
        val filteredCollector = backgroundScope.launch {
            viewModel.filteredTransactions.collectLatest { }
        }
        val statusCollector = backgroundScope.launch {
            viewModel.availableStatuses.collectLatest { }
        }
        val currencyCollector = backgroundScope.launch {
            viewModel.availableCurrencies.collectLatest { }
        }
        advanceUntilIdle()

        assertEquals(listOf("Failed", "Successful"), viewModel.availableStatuses.value)
        assertEquals(listOf("GBP", "USD"), viewModel.availableCurrencies.value)

        viewModel.setStatusFilter(setOf("Successful"))
        viewModel.setCurrencyFilter(setOf("GBP"))
        advanceUntilIdle()

        assertEquals(listOf("gbp_success"), viewModel.filteredTransactions.value.map { it.id })

        viewModel.clearFilters()
        advanceUntilIdle()

        assertEquals(
            listOf("gbp_success", "usd_success", "gbp_failed"),
            viewModel.filteredTransactions.value.map { it.id }
        )
        filteredCollector.cancel()
        statusCollector.cancel()
        currencyCollector.cancel()
    }
}

private class FakeTransactionRepositoryForTest(
    initial: List<Transaction>
) : TransactionRepository {
    private val flow = MutableStateFlow(initial)

    override fun observeTransactions(): Flow<List<Transaction>> = flow

    override suspend fun getTransactions(): List<Transaction> = flow.value

    override suspend fun recordAddMoneySession(
        session: AddMoneySessionData,
        recordedAtMs: Long
    ) = Unit
}

private fun transaction(
    id: String,
    createdAtMs: Long,
    status: String,
    currency: String = "GBP"
): Transaction {
    return Transaction(
        id = id,
        title = id,
        amount = 10.0,
        currency = currency,
        status = status,
        iconRes = 0,
        createdAtMs = createdAtMs
    )
}

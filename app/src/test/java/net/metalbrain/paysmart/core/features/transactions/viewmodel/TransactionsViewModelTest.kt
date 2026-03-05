package net.metalbrain.paysmart.core.features.transactions.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
                    date = "23 Feb 2026",
                    time = "10:30",
                    status = "Successful"
                ),
                transaction(
                    id = "newer",
                    date = "24 Feb 2026",
                    time = "05:52",
                    status = "In Progress"
                )
            )
        )

        val viewModel = TransactionsViewModel(repository)
        val collector = backgroundScope.launch {
            viewModel.filteredTransactions.collectLatest { }
        }
        advanceUntilIdle()

        val ordered = viewModel.filteredTransactions.value
        assertEquals(listOf("newer", "older"), ordered.map { it.id })
        collector.cancel()
    }

    @Test
    fun `status filter keeps newest-first ordering`() = runTest {
        val repository = FakeTransactionRepositoryForTest(
            initial = listOf(
                transaction(
                    id = "success_old",
                    date = "21 Feb 2026",
                    time = "12:00",
                    status = "Successful"
                ),
                transaction(
                    id = "success_new",
                    date = "24 Feb 2026",
                    time = "07:15",
                    status = "Successful"
                ),
                transaction(
                    id = "failed_new",
                    date = "24 Feb 2026",
                    time = "08:00",
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

        val ordered = viewModel.filteredTransactions.value
        assertEquals(listOf("success_new", "success_old"), ordered.map { it.id })
        collector.cancel()
    }
}

private class FakeTransactionRepositoryForTest(
    initial: List<Transaction>
) : TransactionRepository {
    private val flow = MutableStateFlow(initial)

    override fun observeTransactions(): Flow<List<Transaction>> = flow

    override suspend fun getTransactions(): List<Transaction> = flow.value

    override suspend fun upsertAddMoneySimulation(
        sessionId: String,
        amountMinor: Int,
        currency: String,
        status: String,
        createdAtMs: Long
    ) = Unit
}

private fun transaction(
    id: String,
    date: String,
    time: String,
    status: String
): Transaction {
    return Transaction(
        id = id,
        title = id,
        time = time,
        amount = 10.0,
        currency = "GBP",
        status = status,
        date = date,
        iconRes = 0
    )
}

package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeTransactionRepositoryTest {

    @Test
    fun `upsertAddMoneySimulation keeps newest entries at top`() = runTest {
        val repository = FakeTransactionRepository()

        repository.upsertAddMoneySimulation(
            sessionId = "old_session",
            amountMinor = 1_000,
            currency = "GBP",
            status = "pending",
            createdAtMs = 1_700_000_000_000
        )
        repository.upsertAddMoneySimulation(
            sessionId = "new_session",
            amountMinor = 2_000,
            currency = "GBP",
            status = "succeeded",
            createdAtMs = 1_800_000_000_000
        )

        val transactions = repository.getTransactions()

        assertTrue(transactions.isNotEmpty())
        assertEquals("stripe_new_session", transactions.first().id)
    }
}

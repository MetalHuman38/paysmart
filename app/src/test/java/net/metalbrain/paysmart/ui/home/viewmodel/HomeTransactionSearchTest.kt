package net.metalbrain.paysmart.ui.home.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.testing.MainDispatcherRule
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeTransactionSearchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `search query filters by amount after debounce`() = runTest {
        val transactions = MutableStateFlow(
            listOf(
                transaction(
                    id = "stripe_20",
                    createdAtMs = 300L,
                    amount = 20.0,
                    provider = "Stripe"
                ),
                transaction(
                    id = "flutterwave_5",
                    createdAtMs = 200L,
                    amount = 5.0,
                    provider = "Flutterwave"
                ),
            )
        )
        val query = MutableStateFlow("")
        val providers = MutableStateFlow<Set<HomeTransactionProviderFilter>>(emptySet())
        val results = observeHomeTransactionSearch(
            transactions = transactions,
            searchQuery = query,
            selectedProviders = providers,
            debounceMs = 200L,
        ).stateIn(
            scope = backgroundScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeTransactionSearchSnapshot()
        )

        advanceTimeBy(200L)
        runCurrent()
        query.value = "20"
        runCurrent()
        advanceTimeBy(199L)

        assertEquals(
            listOf("stripe_20", "flutterwave_5"),
            results.value.visibleTransactions.map(Transaction::id)
        )

        advanceTimeBy(1L)
        runCurrent()

        assertEquals(listOf("stripe_20"), results.value.visibleTransactions.map(Transaction::id))
    }

    @Test
    fun `provider filter keeps only matching rails`() = runTest {
        val transactions = MutableStateFlow(
            listOf(
                transaction(
                    id = "stripe_20",
                    createdAtMs = 300L,
                    amount = 20.0,
                    provider = "Stripe"
                ),
                transaction(
                    id = "flutterwave_5",
                    createdAtMs = 200L,
                    amount = 5.0,
                    provider = "Flutterwave"
                ),
            )
        )
        val query = MutableStateFlow("")
        val providers = MutableStateFlow<Set<HomeTransactionProviderFilter>>(emptySet())
        val results = observeHomeTransactionSearch(
            transactions = transactions,
            searchQuery = query,
            selectedProviders = providers,
        ).stateIn(
            scope = backgroundScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeTransactionSearchSnapshot()
        )

        advanceTimeBy(200L)
        runCurrent()
        providers.value = setOf(HomeTransactionProviderFilter.FLUTTERWAVE)
        runCurrent()

        assertEquals(
            listOf("flutterwave_5"),
            results.value.visibleTransactions.map(Transaction::id)
        )
        assertEquals(
            setOf(HomeTransactionProviderFilter.FLUTTERWAVE),
            results.value.selectedProviders
        )
    }
}

private fun transaction(
    id: String,
    createdAtMs: Long,
    amount: Double,
    provider: String,
): Transaction {
    return Transaction(
        id = id,
        title = id,
        amount = amount,
        currency = "GBP",
        status = "Successful",
        iconRes = 0,
        createdAtMs = createdAtMs,
        provider = provider
    )
}

package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.repository.AddMoneyRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.WalletBalanceModel
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.testing.MainDispatcherRule
import net.metalbrain.paysmart.navigator.Screen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState prefers requested currency when it exists in wallet balances`() = runTest {
        val addMoneyRepository = mockk<AddMoneyRepository>()
        val walletBalanceRepository = mockk<WalletBalanceRepository>()
        val transactionRepository = mockk<TransactionRepository>()
        val userManager = mockk<UserManager>()

        every { userManager.authState } returns flowOf(AuthState.Authenticated("uid-1"))
        coEvery {
            addMoneyRepository.getSessionStatus(any(), AddMoneyProvider.FLUTTERWAVE)
        } returns Result.failure(IllegalStateException("No Flutterwave session expected in this test"))
        every { walletBalanceRepository.observeByUserId("uid-1") } returns flowOf(
            WalletBalanceModel(
                userId = "uid-1",
                balancesByCurrency = mapOf(
                    "GBP" to 120.0,
                    "NGN" to 48000.0
                )
            )
        )
        every { transactionRepository.observeTransactions() } returns flowOf(
            listOf(
                sampleTransaction(id = "gbp-1", currency = "GBP", amount = 12.0),
                sampleTransaction(id = "ngn-1", currency = "NGN", amount = 1500.0)
            )
        )

        val viewModel = BalanceDetailsViewModel(
            addMoneyRepository = addMoneyRepository,
            walletBalanceRepository = walletBalanceRepository,
            transactionRepository = transactionRepository,
            userManager = userManager,
            savedStateHandle = SavedStateHandle(
                mapOf(Screen.BalanceDetails.CURRENCYARG to "NGN")
            )
        )

        val collector = backgroundScope.launch {
            viewModel.uiState.collectLatest { }
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("NGN", state.currencyCode)
        assertEquals(48000.0, state.amount, 0.0)
        assertEquals(listOf("NGN"), state.recentTransactions.map { it.currency })

        collector.cancel()
        advanceTimeBy(5_000)
        advanceUntilIdle()
    }

    private fun sampleTransaction(
        id: String,
        currency: String,
        amount: Double
    ): Transaction {
        return Transaction(
            id = id,
            title = "Transaction $id",
            amount = amount,
            currency = currency,
            status = "COMPLETED",
            iconRes = 0,
            createdAtMs = 1_700_000_000_000L
        )
    }
}

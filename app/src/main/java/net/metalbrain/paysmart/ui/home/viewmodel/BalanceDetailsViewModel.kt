package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.home.state.BalanceDetailsUiState
import net.metalbrain.paysmart.ui.home.support.balanceAmountForCurrency
import net.metalbrain.paysmart.ui.home.support.resolvePrimaryBalanceCurrency

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class BalanceDetailsViewModel @Inject constructor(
    private val walletBalanceRepository: WalletBalanceRepository,
    private val transactionRepository: TransactionRepository,
    userManager: UserManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val preferredCurrencyCode = savedStateHandle
        .get<String>(Screen.BalanceDetails.CURRENCYARG)
        .orEmpty()
        .trim()

    val uiState = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> combine(
                    walletBalanceRepository.observeByUserId(auth.uid),
                    transactionRepository.observeTransactions()
                ) { wallet, transactions ->
                    val balances = wallet?.balancesByCurrency.orEmpty()
                    val currencyCode = resolvePrimaryBalanceCurrency(
                        balancesByCurrency = balances,
                        preferredCurrencyCode = preferredCurrencyCode
                    )

                    BalanceDetailsUiState(
                        isLoading = false,
                        currencyCode = currencyCode,
                        amount = balances.balanceAmountForCurrency(currencyCode),
                        balancesByCurrency = balances,
                        recentTransactions = transactions
                            .filter { transaction ->
                                transaction.currency.equals(currencyCode, ignoreCase = true)
                            }
                            .sortedByDescending { transaction -> transaction.createdAtMs },
                        walletUpdatedAtMs = wallet?.updatedAtMs
                    )
                }

                else -> flowOf(BalanceDetailsUiState(isLoading = false))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BalanceDetailsUiState()
        )
}








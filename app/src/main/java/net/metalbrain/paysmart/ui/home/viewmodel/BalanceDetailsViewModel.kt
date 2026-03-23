package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.core.features.addmoney.repository.AddMoneyRepository
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.ui.home.state.BalanceDetailsUiState
import net.metalbrain.paysmart.ui.home.support.balanceAmountForCurrency
import net.metalbrain.paysmart.ui.home.support.resolvePrimaryBalanceCurrency

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class BalanceDetailsViewModel @Inject constructor(
    private val addMoneyRepository: AddMoneyRepository,
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
                is AuthState.Authenticated -> {
                    val baseFlow = combine(
                        walletBalanceRepository.observeByUserId(auth.uid),
                        transactionRepository.observeTransactions()
                    ) { wallet, transactions ->
                        val balances = wallet?.balancesByCurrency.orEmpty()
                        val currencyCode = resolvePrimaryBalanceCurrency(
                            balancesByCurrency = balances,
                            preferredCurrencyCode = preferredCurrencyCode
                        )

                        BalanceDetailsBaseState(
                            currencyCode = currencyCode,
                            balancesByCurrency = balances,
                            recentTransactions = transactions
                                .filter { transaction ->
                                    transaction.currency.equals(currencyCode, ignoreCase = true)
                                }
                                .sortedByDescending { transaction -> transaction.createdAtMs },
                            walletUpdatedAtMs = wallet?.updatedAtMs
                        )
                    }

                    val flutterwaveTopupFlow = baseFlow
                        .map { base ->
                            resolveActiveFlutterwaveTopupSessionId(
                                currencyCode = base.currencyCode,
                                transactions = base.recentTransactions
                            )
                        }
                        .distinctUntilChanged()
                        .flatMapLatest { sessionId ->
                            if (sessionId.isNullOrBlank()) {
                                flowOf(FlutterwaveTopupDetailsState())
                            } else {
                                flow {
                                    emit(FlutterwaveTopupDetailsState(isLoading = true))
                                    val session = addMoneyRepository
                                        .getSessionStatus(sessionId, AddMoneyProvider.FLUTTERWAVE)
                                        .getOrNull()
                                        ?.takeIf { topup ->
                                            topup.virtualAccount != null &&
                                                (topup.status == AddMoneySessionStatus.CREATED ||
                                                    topup.status == AddMoneySessionStatus.PENDING)
                                        }
                                    emit(
                                        FlutterwaveTopupDetailsState(
                                            isLoading = false,
                                            session = session
                                        )
                                    )
                                }
                            }
                        }

                    combine(baseFlow, flutterwaveTopupFlow) { base, topup ->
                        BalanceDetailsUiState(
                            isLoading = false,
                            currencyCode = base.currencyCode,
                            amount = base.balancesByCurrency.balanceAmountForCurrency(base.currencyCode),
                            balancesByCurrency = base.balancesByCurrency,
                            recentTransactions = base.recentTransactions,
                            accountDetailsLoading = topup.isLoading,
                            activeFlutterwaveTopup = topup.session,
                            walletUpdatedAtMs = base.walletUpdatedAtMs
                        )
                    }
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

private data class BalanceDetailsBaseState(
    val currencyCode: String,
    val balancesByCurrency: Map<String, Double>,
    val recentTransactions: List<Transaction>,
    val walletUpdatedAtMs: Long?
)

private data class FlutterwaveTopupDetailsState(
    val isLoading: Boolean = false,
    val session: AddMoneySessionData? = null
)

private fun resolveActiveFlutterwaveTopupSessionId(
    currencyCode: String,
    transactions: List<Transaction>
): String? {
    return transactions.firstOrNull { transaction ->
        transaction.currency.equals(currencyCode, ignoreCase = true) &&
            transaction.provider.equals("Flutterwave", ignoreCase = true) &&
            transaction.title.equals("Top up via Flutterwave", ignoreCase = true) &&
            transaction.status.equals("In Progress", ignoreCase = true)
    }?.id
}

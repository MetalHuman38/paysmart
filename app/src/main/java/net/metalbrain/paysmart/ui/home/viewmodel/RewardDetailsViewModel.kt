package net.metalbrain.paysmart.ui.home.viewmodel

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
import net.metalbrain.paysmart.ui.home.state.RewardDetailsUiState

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RewardDetailsViewModel @Inject constructor(
    private val walletBalanceRepository: WalletBalanceRepository,
    private val transactionRepository: TransactionRepository,
    userManager: UserManager
) : ViewModel() {

    val uiState = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> combine(
                    walletBalanceRepository.observeByUserId(auth.uid),
                    transactionRepository.observeTransactions()
                ) { wallet, transactions ->
                    RewardDetailsUiState(
                        isLoading = false,
                        points = wallet?.rewardsPoints ?: 0.0,
                        walletUpdatedAtMs = wallet?.updatedAtMs,
                        recentTransactions = transactions
                            .sortedByDescending { transaction -> transaction.createdAtMs }
                            .take(8)
                    )
                }

                else -> flowOf(RewardDetailsUiState(isLoading = false))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RewardDetailsUiState()
        )
}

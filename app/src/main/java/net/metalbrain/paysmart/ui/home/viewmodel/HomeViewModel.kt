package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeUiState

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    securityPreference: SecurityPreference,
    private val walletBalanceRepository: WalletBalanceRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _transactions = MutableStateFlow(emptyList<Transaction>())

    private val walletBalances = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> walletBalanceRepository.observeByUserId(auth.uid)
                else -> flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = null
        )

    val uiState: StateFlow<HomeUiState> = combine(
        securityPreference.localSecurityStateFlow,
        _transactions,
        walletBalances
    ) { localSecurity, transactions, wallet ->

        HomeUiState(
            security = localSecurity,
            recentTransactions = transactions.take(3),
            balanceSnapshot = HomeBalanceSnapshot(
                balancesByCurrency = wallet?.balancesByCurrency ?: emptyMap(),
                rewardsPoints = wallet?.rewardsPoints ?: 0.0
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        refresh()
        syncWalletOnAuth()
    }

    fun refresh() {
        viewModelScope.launch {
            _transactions.value = transactionRepository.getTransactions()
        }
    }

    private fun syncWalletOnAuth() {
        viewModelScope.launch {
            userManager.authState.collect { auth ->
                if (auth is AuthState.Authenticated) {
                    walletBalanceRepository.syncFromServer(auth.uid)
                }
            }
        }
    }
}

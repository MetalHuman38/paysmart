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
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeUiState
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    securityPreference: SecurityPreference,
    private val walletBalanceRepository: WalletBalanceRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val userManager: UserManager
) : ViewModel() {
    private val transactions = transactionRepository.observeTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val walletBalances = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> walletBalanceRepository.observeByUserId(auth.uid)
                else -> flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val countryCapabilities = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> profileCacheRepository.observeByUid(auth.uid)
                    .flatMapLatest { profile ->
                        countryCapabilityRepository.observeProfile(profile?.country)
                    }

                else -> countryCapabilityRepository.observeProfile(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryCapabilityCatalog.defaultProfile()
        )

    val uiState: StateFlow<HomeUiState> = combine(
        securityPreference.localSecurityStateFlow,
        transactions,
        walletBalances,
        countryCapabilities
    ) { localSecurity, transactions, wallet, capabilityProfile ->

        HomeUiState(
            security = localSecurity,
            recentTransactions = transactions.take(3),
            balanceSnapshot = HomeBalanceSnapshot(
                balancesByCurrency = wallet?.balancesByCurrency ?: emptyMap()
            ),
            rewardEarned = RewardEarnedSnapshot(
                points = wallet?.rewardsPoints ?: 0.0
            ),
            countryFlagEmoji = capabilityProfile.flagEmoji,
            topUpPolicyHint = CountryCapabilityCatalog.topUpPolicyHint(capabilityProfile)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        syncWalletOnAuth()
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

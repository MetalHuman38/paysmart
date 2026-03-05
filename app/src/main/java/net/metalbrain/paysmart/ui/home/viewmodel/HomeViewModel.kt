package net.metalbrain.paysmart.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
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
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeUiState
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    securityPreference: SecurityPreference,
    private val walletBalanceRepository: WalletBalanceRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val fxQuoteRepository: FxQuoteRepository,
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

    private val exchangeRateSnapshot: StateFlow<HomeExchangeRateSnapshot> = countryCapabilities
        .flatMapLatest { profile ->
            observeExchangeRate(profile)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeExchangeRateSnapshot()
        )

    val uiState: StateFlow<HomeUiState> = combine(
        securityPreference.localSecurityStateFlow,
        transactions,
        walletBalances,
        countryCapabilities,
        exchangeRateSnapshot
    ) { localSecurity, transactions, wallet, capabilityProfile, exchangeRate ->
        val sortedTransactions = transactions.sortedByDescending { transaction ->
            transaction.toSortEpochMillis()
        }

        HomeUiState(
            security = localSecurity,
            recentTransactions = sortedTransactions.take(3),
            balanceSnapshot = HomeBalanceSnapshot(
                balancesByCurrency = wallet?.balancesByCurrency ?: emptyMap(),
                preferredCurrencyCode = capabilityProfile.currencyCode
            ),
            rewardEarned = RewardEarnedSnapshot(
                points = wallet?.rewardsPoints ?: 0.0
            ),
            countryFlagEmoji = capabilityProfile.flagEmoji,
            countryCurrencyCode = capabilityProfile.currencyCode,
            topUpPolicyHint = CountryCapabilityCatalog.topUpPolicyHint(capabilityProfile),
            capabilities = capabilityProfile.capabilities,
            exchangeRateSnapshot = exchangeRate
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

    private fun observeExchangeRate(profile: CountryCapabilityProfile): Flow<HomeExchangeRateSnapshot> = flow {
        val baseCurrency = CountryCapabilityCatalog.defaultProfile().currencyCode
            .trim()
            .uppercase(Locale.US)
        val targetCurrency = profile.currencyCode
            .trim()
            .uppercase(Locale.US)
            .ifBlank { baseCurrency }

        if (baseCurrency == targetCurrency) {
            emit(
                HomeExchangeRateSnapshot(
                    baseCurrencyCode = baseCurrency,
                    targetCurrencyCode = targetCurrency,
                    rate = 1.0,
                    isLoading = false
                )
            )
            return@flow
        }

        emit(
            HomeExchangeRateSnapshot(
                baseCurrencyCode = baseCurrency,
                targetCurrencyCode = targetCurrency,
                isLoading = true
            )
        )

        val paymentMethod = profile.addMoneyMethods.firstOrNull() ?: FxPaymentMethod.DEBIT_CARD
        val query = FxQuoteQuery(
            sourceCurrency = baseCurrency,
            targetCurrency = targetCurrency,
            sourceAmount = 1.0,
            method = paymentMethod
        )

        val nextState = fxQuoteRepository.getQuote(query).fold(
            onSuccess = { result ->
                HomeExchangeRateSnapshot(
                    baseCurrencyCode = baseCurrency,
                    targetCurrencyCode = targetCurrency,
                    rate = result.quote.rate,
                    dataSource = result.dataSource,
                    isLoading = false
                )
            },
            onFailure = { throwable ->
                HomeExchangeRateSnapshot(
                    baseCurrencyCode = baseCurrency,
                    targetCurrencyCode = targetCurrency,
                    isLoading = false,
                    errorMessage = throwable.localizedMessage ?: "Unable to load exchange rate"
                )
            }
        )

        emit(nextState)
    }
}

private val HOME_TRANSACTION_SORT_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.US)

private fun Transaction.toSortEpochMillis(): Long {
    val rawDateTime = "${date.trim()} ${time.trim()}"
    return runCatching {
        LocalDateTime.parse(rawDateTime, HOME_TRANSACTION_SORT_FORMAT)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrDefault(0L)
}

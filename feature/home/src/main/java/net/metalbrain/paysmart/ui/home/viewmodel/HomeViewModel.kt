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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceGateway
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.ui.home.data.HomeCountryCapabilityGateway
import net.metalbrain.paysmart.ui.home.data.HomeNotificationGateway
import net.metalbrain.paysmart.ui.home.data.HomeNotificationMessage
import net.metalbrain.paysmart.ui.home.data.HomeRecentRecipientGateway
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeNotificationKind
import net.metalbrain.paysmart.ui.home.state.HomeNotificationUiState
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import net.metalbrain.paysmart.ui.home.state.HomeUiState
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import java.util.Locale

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    private val securityPreference: SecurityPreference,
    private val walletBalanceRepository: WalletBalanceGateway,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityGateway: HomeCountryCapabilityGateway,
    private val fxQuoteRepository: FxQuoteRepository,
    private val userManager: UserManager,
    notificationGateway: HomeNotificationGateway,
    recentRecipientGateway: HomeRecentRecipientGateway,
) : ViewModel() {
    private val transactions = transactionRepository.observeTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val transactionSearchQuery = MutableStateFlow("")
    private val selectedTransactionProviders =
        MutableStateFlow<Set<HomeTransactionProviderFilter>>(emptySet())

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
                        countryCapabilityGateway.observeProfile(profile?.country)
                    }

                else -> countryCapabilityGateway.observeProfile(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryCapabilityCatalog.defaultProfile()
        )

    private val profile = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> profileCacheRepository.observeByUid(auth.uid)
                else -> flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
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

    private val transactionSearch = observeHomeTransactionSearch(
        transactions = transactions,
        searchQuery = transactionSearchQuery,
        selectedProviders = selectedTransactionProviders,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeTransactionSearchSnapshot()
    )

    private val headerState = combine(
        profile,
        transactionSearch,
        notificationGateway.observeLatestNotification(),
        notificationGateway.observeUnreadCount(),
    ) { profile, transactionSearch, latestNotification, unreadCount ->
        HomeHeaderState(
            displayName = profile?.displayName.orEmpty(),
            launchInterest = profile?.launchInterest,
            transactionSearch = transactionSearch,
            notification = resolveNotification(
                latestNotification = latestNotification,
                unreadCount = unreadCount
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeHeaderState()
    )

    private val recentRecipients = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> recentRecipientGateway.observeRecentByUserId(auth.uid)
                else -> flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val homeHeaderAndRecipients = combine(
        headerState,
        recentRecipients
    ) { header, recipients ->
        header to recipients
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeHeaderState() to emptyList()
    )

    val uiState: StateFlow<HomeUiState> = combine(
        securityPreference.localSecurityStateFlow,
        walletBalances,
        countryCapabilities,
        exchangeRateSnapshot,
        homeHeaderAndRecipients,
    ) { localSecurity, wallet, capabilityProfile, exchangeRate, homeHeaderAndRecipients ->
        val (headerState, recentRecipients) = homeHeaderAndRecipients
        HomeUiState(
            security = localSecurity,
            displayName = headerState.displayName,
            recentTransactions = headerState.transactionSearch.visibleTransactions,
            recentRecipients = recentRecipients,
            transactionSearchQuery = headerState.transactionSearch.searchQuery,
            isTransactionSearchActive = headerState.transactionSearch.isSearchActive,
            availableTransactionProviders = headerState.transactionSearch.availableProviders,
            selectedTransactionProviders = headerState.transactionSearch.selectedProviders,
            notification = headerState.notification,
            balanceSnapshot = HomeBalanceSnapshot(
                balancesByCurrency = wallet?.balancesByCurrency ?: emptyMap(),
                preferredCurrencyCode = capabilityProfile.currencyCode
            ),
            rewardEarned = RewardEarnedSnapshot(
                points = wallet?.rewardsPoints ?: 0.0
            ),
            countryIso2 = capabilityProfile.iso2,
            countryFlagEmoji = capabilityProfile.flagEmoji,
            countryCurrencyCode = capabilityProfile.currencyCode,
            launchInterest = headerState.launchInterest
                ?: LaunchInterest.defaultForCountry(capabilityProfile.iso2),
            topUpPolicyHint = CountryCapabilityCatalog.topUpPolicyHint(capabilityProfile),
            capabilities = capabilityProfile.capabilities,
            exchangeRateSnapshot = exchangeRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    val hideBalanceEnabled: StateFlow<Boolean> =
        securityPreference.hideBalanceFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    init {
        syncWalletOnAuth()
    }

    fun onTransactionSearchQueryChanged(value: String) {
        transactionSearchQuery.value = value
    }

    fun onTransactionProviderToggled(provider: HomeTransactionProviderFilter) {
        val current = selectedTransactionProviders.value
        selectedTransactionProviders.value = if (provider in current) {
            current - provider
        } else {
            current + provider
        }
    }

    fun onToggleBalanceVisibility() {
        viewModelScope.launch {
            securityPreference.setHideBalance(!hideBalanceEnabled.value)
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

private fun resolveNotification(
    latestNotification: HomeNotificationMessage?,
    unreadCount: Int,
): HomeNotificationUiState {
    return if (latestNotification != null) {
        HomeNotificationUiState(
            kind = if (latestNotification.type == HomeNotificationGateway.TYPE_APP_UPDATE_READY) {
                HomeNotificationKind.APP_UPDATE_READY
            } else {
                HomeNotificationKind.INBOX
            },
            title = latestNotification.title,
            body = latestNotification.body,
            unreadCount = unreadCount,
            isUnread = unreadCount > 0 || latestNotification.isUnread,
        )
    } else {
        HomeNotificationUiState(kind = HomeNotificationKind.PLACEHOLDER)
    }
}

private data class HomeHeaderState(
    val displayName: String = "",
    val launchInterest: LaunchInterest? = null,
    val transactionSearch: HomeTransactionSearchSnapshot = HomeTransactionSearchSnapshot(),
    val notification: HomeNotificationUiState = HomeNotificationUiState(),
)

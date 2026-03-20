package net.metalbrain.paysmart.core.features.capabilities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountTypeAndLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitSelectorRowUiState
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitsListUiState
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AccountLimitsListViewModel @Inject constructor(
    private val walletBalanceRepository: WalletBalanceRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val accountTypeAndLimitCatalog: AccountTypeAndLimitCatalog,
    userManager: UserManager
) : ViewModel() {

    private val preferredMarket = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> profileCacheRepository.observeByUid(auth.uid)
                    .flatMapLatest { profile ->
                        countryCapabilityRepository.observeProfile(profile?.country)
                    }

                else -> flowOf(CountryCapabilityCatalog.defaultProfile())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryCapabilityCatalog.defaultProfile()
        )

    val uiState = userManager.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.Authenticated -> combine(
                    walletBalanceRepository.observeByUserId(auth.uid),
                    preferredMarket
                ) { wallet, homeMarket ->
                    AccountLimitsListUiState(
                        isLoading = false,
                        accounts = wallet?.balancesByCurrency
                            .orEmpty()
                            .keys
                            .map { it.trim().uppercase(Locale.US) }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .map { currencyCode ->
                                val marketProfile = accountTypeAndLimitCatalog.resolveMarketForCurrency(
                                    rawCurrencyCode = currencyCode,
                                    preferredIso2 = homeMarket.iso2
                                )

                                AccountLimitSelectorRowUiState(
                                    currencyCode = currencyCode,
                                    currencyName = accountTypeAndLimitCatalog.currencyDisplayName(currencyCode),
                                    accountDescriptor = accountTypeAndLimitCatalog.accountDescriptor(marketProfile),
                                    flagEmoji = marketProfile?.flagEmoji ?: "\uD83C\uDF0D",
                                    marketIso2 = marketProfile?.iso2.orEmpty()
                                )
                            }
                            .sortedWith(
                                compareByDescending<AccountLimitSelectorRowUiState> { row ->
                                    row.marketIso2.equals(homeMarket.iso2, ignoreCase = true)
                                }.thenBy { row ->
                                    row.currencyName.lowercase(Locale.US)
                                }
                            )
                    )
                }

                else -> flowOf(AccountLimitsListUiState(isLoading = false))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountLimitsListUiState()
        )
}

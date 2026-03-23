package net.metalbrain.paysmart.core.features.capabilities.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitMarketProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitValueCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountTypeAndLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.mapper.AccountLimitDetailsUiMapper
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryAccountLimitRepository
import net.metalbrain.paysmart.core.features.capabilities.repository.CountryCapabilityRepository
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitDetailsUiState
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.navigator.Screen

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AccountLimitDetailsViewModel @Inject constructor(
    private val countryAccountLimitRepository: CountryAccountLimitRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
    private val countryCapabilityRepository: CountryCapabilityRepository,
    private val accountTypeAndLimitCatalog: AccountTypeAndLimitCatalog,
    private val accountLimitValueCatalog: AccountLimitValueCatalog,
    @param:ApplicationContext private val context: Context,
    userManager: UserManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val requestedCurrencyCode = savedStateHandle
        .get<String>(Screen.ProfileAccountLimitsDetails.CURRENCYARG)
        .orEmpty()
        .trim()
        .uppercase(Locale.US)
        .ifBlank { AccountLimitCatalog.defaultProfile().currencyCode }

    private val selectedTab = MutableStateFlow(AccountLimitKey.SEND)

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

    private val profileWithMarket = preferredMarket
        .flatMapLatest { preferred ->
            val resolvedMarket = resolveMarketProfile(preferred.iso2)
            countryAccountLimitRepository.observeProfile(resolvedMarket.iso2)
                .map { profile ->
                    profile to (
                        accountTypeAndLimitCatalog.resolveMarketForCurrency(
                            rawCurrencyCode = requestedCurrencyCode,
                            preferredIso2 = preferred.iso2
                        ) ?: accountTypeAndLimitCatalog.profileForIso2(profile.iso2)
                            ?: resolvedMarket
                        )
                }
        }

    val uiState = combine(profileWithMarket, selectedTab) { profileAndMarket, activeTab ->
        val (profile, marketProfile) = profileAndMarket
        val tabs = AccountLimitDetailsUiMapper.resolveTabs(profile)
        val resolvedTab = AccountLimitDetailsUiMapper.resolveSelectedTab(tabs, activeTab)

        AccountLimitDetailsUiState(
            isLoading = false,
            currencyCode = marketProfile.currencyCode.ifBlank { requestedCurrencyCode },
            flagEmoji = marketProfile.flagEmoji,
            subtitle = context.getString(
                R.string.account_limits_details_subtitle_format,
                marketProfile.currencyCode.ifBlank { requestedCurrencyCode }
            ),
            tabs = tabs,
            selectedTab = resolvedTab,
            cards = AccountLimitDetailsUiMapper.buildCards(
                profile = profile.copy(tabs = tabs),
                marketProfile = marketProfile,
                selectedTab = resolvedTab,
                valueProfile = accountLimitValueCatalog.valuesForIso2(marketProfile.iso2),
                spentOfLimitFormat = { spent, limit ->
                    context.getString(R.string.account_limits_spent_of_limit_format, spent, limit)
                },
                leftFormat = { limit ->
                    context.getString(R.string.account_limits_left_format, limit)
                }
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountLimitDetailsUiState(
            currencyCode = requestedCurrencyCode
        )
    )

    fun onTabSelected(key: AccountLimitKey) {
        selectedTab.value = key
    }

    private fun resolveMarketProfile(preferredIso2: String): AccountLimitMarketProfile {
        return accountTypeAndLimitCatalog.resolveMarketForCurrency(
            rawCurrencyCode = requestedCurrencyCode,
            preferredIso2 = preferredIso2
        ) ?: accountTypeAndLimitCatalog.profileForIso2(preferredIso2)
            ?: accountTypeAndLimitCatalog.profileForIso2(AccountLimitCatalog.DEFAULT_ISO2)
            ?: AccountLimitMarketProfile(
                iso2 = AccountLimitCatalog.DEFAULT_ISO2,
                countryName = AccountLimitCatalog.defaultProfile().countryName,
                flagEmoji = AccountLimitCatalog.defaultProfile().flagEmoji,
                currencyCode = requestedCurrencyCode,
                currencyName = requestedCurrencyCode,
                currencySymbol = requestedCurrencyCode,
                supportsIban = false,
                supportsLocalAccount = false
            )
    }
}

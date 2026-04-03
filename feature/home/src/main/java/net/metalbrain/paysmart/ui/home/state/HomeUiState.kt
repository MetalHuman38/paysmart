package net.metalbrain.paysmart.ui.home.state

import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource

data class HomeBalanceSnapshot(
    val balancesByCurrency: Map<String, Double> = emptyMap(),
    val preferredCurrencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode
)

data class HomeExchangeRateSnapshot(
    val baseCurrencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val targetCurrencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val rate: Double? = null,
    val dataSource: FxQuoteDataSource? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class HomeUiState(
    val security: LocalSecuritySettingsModel? = null,
    val displayName: String = "",
    val recentTransactions: List<Transaction> = emptyList(),
    val recentRecipients: List<HomeRecentRecipient> = emptyList(),
    val transactionSearchQuery: String = "",
    val isTransactionSearchActive: Boolean = false,
    val availableTransactionProviders: List<HomeTransactionProviderFilter> = emptyList(),
    val selectedTransactionProviders: Set<HomeTransactionProviderFilter> = emptySet(),
    val notification: HomeNotificationUiState = HomeNotificationUiState(),
    val balanceSnapshot: HomeBalanceSnapshot = HomeBalanceSnapshot(),
    val rewardEarned: RewardEarnedSnapshot = RewardEarnedSnapshot(),
    val countryIso2: String = CountryCapabilityCatalog.defaultProfile().iso2,
    val countryFlagEmoji: String = "🌍",
    val countryCurrencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val launchInterest: LaunchInterest = LaunchInterest.defaultForCountry(
        CountryCapabilityCatalog.defaultProfile().iso2
    ),
    val topUpPolicyHint: String? = null,
    val capabilities: List<CapabilityItem> = emptyList(),
    val exchangeRateSnapshot: HomeExchangeRateSnapshot = HomeExchangeRateSnapshot()
)

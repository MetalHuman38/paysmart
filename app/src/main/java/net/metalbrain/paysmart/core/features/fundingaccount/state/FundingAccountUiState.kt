package net.metalbrain.paysmart.core.features.fundingaccount.state

import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountErrorCode

enum class FundingAccountScreenPhase {
    LOADING,
    READY,
    PENDING,
    EMPTY,
    UNSUPPORTED_MARKET,
    KYC_REQUIRED,
    ERROR
}

data class FundingAccountUiState(
    val account: FundingAccountData? = null,
    val phase: FundingAccountScreenPhase = FundingAccountScreenPhase.LOADING,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isProvisioning: Boolean = false,
    val isMarketSupported: Boolean = false,
    val countryIso2: String = CountryCapabilityCatalog.defaultProfile().iso2,
    val countryName: String = CountryCapabilityCatalog.defaultProfile().countryName,
    val countryFlagEmoji: String = CountryCapabilityCatalog.defaultProfile().flagEmoji,
    val currencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val provider: String = "flutterwave",
    val lastErrorCode: FundingAccountErrorCode? = null
) {
    val canProvision: Boolean
        get() = isMarketSupported &&
            !isProvisioning &&
            phase in setOf(
                FundingAccountScreenPhase.EMPTY,
                FundingAccountScreenPhase.ERROR
            )

    val showDetails: Boolean
        get() = account != null
}

package net.metalbrain.paysmart.core.features.fx.state

import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog

data class ExchangeRateMarketUiState(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val targetCurrencyCode: String,
    val rate: Double? = null
)

data class ExchangeRatesUiState(
    val baseCurrencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val items: List<ExchangeRateMarketUiState> = emptyList(),
    val isLoading: Boolean = true,
    val allUnavailable: Boolean = false
)

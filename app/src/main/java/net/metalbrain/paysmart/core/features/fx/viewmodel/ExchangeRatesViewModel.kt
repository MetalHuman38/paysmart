package net.metalbrain.paysmart.core.features.fx.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionItem
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.repository.FxQuoteRepository
import net.metalbrain.paysmart.core.features.fx.state.ExchangeRateMarketUiState
import net.metalbrain.paysmart.core.features.fx.state.ExchangeRatesUiState
import net.metalbrain.paysmart.navigator.Screen

@HiltViewModel
class ExchangeRatesViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val fxQuoteRepository: FxQuoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val priorityCountryIso2 = savedStateHandle
        .get<String>(Screen.ExchangeRates.COUNTRY_ISO2_ARG)
        .orEmpty()
        .trim()
        .uppercase(Locale.US)

    private val _uiState = MutableStateFlow(ExchangeRatesUiState())
    val uiState: StateFlow<ExchangeRatesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadRates()
        }
    }

    private suspend fun loadRates() {
        val baseCurrencyCode = CountryCapabilityCatalog.defaultProfile().currencyCode
            .trim()
            .uppercase(Locale.US)
        val markets = buildMarkets(baseCurrencyCode)

        _uiState.value = ExchangeRatesUiState(
            baseCurrencyCode = baseCurrencyCode,
            isLoading = true
        )

        val items = coroutineScope {
            markets.map { market ->
                async {
                    val query = FxQuoteQuery(
                        sourceCurrency = baseCurrencyCode,
                        targetCurrency = market.currencyCode,
                        sourceAmount = 1.0,
                        method = FxPaymentMethod.WIRE
                    )
                    fxQuoteRepository.getQuote(query).fold(
                        onSuccess = { result ->
                            ExchangeRateMarketUiState(
                                iso2 = market.iso2,
                                countryName = market.name,
                                flagEmoji = market.flagEmoji,
                                targetCurrencyCode = market.currencyCode,
                                rate = result.quote.rate
                            )
                        },
                        onFailure = {
                            ExchangeRateMarketUiState(
                                iso2 = market.iso2,
                                countryName = market.name,
                                flagEmoji = market.flagEmoji,
                                targetCurrencyCode = market.currencyCode,
                                rate = null
                            )
                        }
                    )
                }
            }.awaitAll()
        }

        _uiState.update {
            it.copy(
                items = items,
                isLoading = false,
                allUnavailable = items.all { item -> item.rate == null }
            )
        }
    }

    private fun buildMarkets(baseCurrencyCode: String): List<CountrySelectionItem> {
        val countries = CountrySelectionCatalog.countries(appContext)
        val byIso = countries.associateBy { country -> country.iso2 }
        val seenCurrencies = mutableSetOf(baseCurrencyCode)
        val selected = mutableListOf<CountrySelectionItem>()

        val priorityIso2s = buildList {
            if (priorityCountryIso2.length == 2) {
                add(priorityCountryIso2)
            }
            addAll(PRIORITY_CORRIDOR_ISO2S)
        }

        priorityIso2s.forEach { iso2 ->
            val country = byIso[iso2] ?: return@forEach
            val currencyCode = country.currencyCode.trim().uppercase(Locale.US)
            if (currencyCode.length == 3 && seenCurrencies.add(currencyCode)) {
                selected += country
            }
        }

        countries.forEach { country ->
            if (selected.size >= MAX_MARKETS) return@forEach
            val currencyCode = country.currencyCode.trim().uppercase(Locale.US)
            if (currencyCode.length == 3 && seenCurrencies.add(currencyCode)) {
                selected += country
            }
        }

        return selected.take(MAX_MARKETS)
    }

    private companion object {
        const val MAX_MARKETS = 10
        val PRIORITY_CORRIDOR_ISO2S = listOf("NG", "LK", "EG", "MA", "SN", "CN", "US", "GM", "ET", "NP")
    }
}

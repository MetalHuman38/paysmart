package net.metalbrain.paysmart.ui.home.state

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.domain.model.Transaction

data class BalanceDetailsUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = CountryCapabilityCatalog.defaultProfile().currencyCode,
    val amount: Double = 0.0,
    val balancesByCurrency: Map<String, Double> = emptyMap(),
    val recentTransactions: List<Transaction> = emptyList(),
    val accountDetailsLoading: Boolean = false,
    val activeFlutterwaveTopup: AddMoneySessionData? = null,
    val walletUpdatedAtMs: Long? = null
)

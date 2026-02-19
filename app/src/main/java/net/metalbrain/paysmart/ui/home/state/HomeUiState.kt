package net.metalbrain.paysmart.ui.home.state

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction

data class HomeBalanceSnapshot(
    val balancesByCurrency: Map<String, Double> = emptyMap(),
    val rewardsPoints: Double = 0.0
)

data class HomeUiState(
    val security: LocalSecuritySettingsModel? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val balanceSnapshot: HomeBalanceSnapshot = HomeBalanceSnapshot()
)

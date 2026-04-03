package net.metalbrain.paysmart.ui.home.state

import net.metalbrain.paysmart.domain.model.Transaction

data class RewardDetailsUiState(
    val isLoading: Boolean = true,
    val points: Double = 0.0,
    val walletUpdatedAtMs: Long? = null,
    val recentTransactions: List<Transaction> = emptyList()
)

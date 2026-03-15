package net.metalbrain.paysmart.core.features.transactions.state

import net.metalbrain.paysmart.domain.model.Transaction

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null
)

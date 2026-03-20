package net.metalbrain.paysmart.core.features.capabilities.state

data class AccountLimitSelectorRowUiState(
    val currencyCode: String,
    val currencyName: String,
    val accountDescriptor: String,
    val flagEmoji: String,
    val marketIso2: String
)

data class AccountLimitsListUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountLimitSelectorRowUiState> = emptyList()
)

package net.metalbrain.paysmart.core.features.capabilities.state

import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitTabSpec

data class AccountLimitCardUiState(
    val key: String,
    val title: String,
    val progress: Float,
    val leadingLabel: String,
    val trailingLabel: String
)

data class AccountLimitDetailsUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = AccountLimitCatalog.defaultProfile().currencyCode,
    val flagEmoji: String = AccountLimitCatalog.defaultProfile().flagEmoji,
    val subtitle: String = "",
    val tabs: List<AccountLimitTabSpec> = AccountLimitCatalog.defaultTabs(),
    val selectedTab: AccountLimitKey = AccountLimitKey.SEND,
    val cards: List<AccountLimitCardUiState> = emptyList()
)

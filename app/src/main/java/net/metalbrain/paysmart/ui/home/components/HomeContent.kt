package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.components.AccountsHeader
import net.metalbrain.paysmart.core.features.account.profile.card.ProfileCompletionCard
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.home.card.AccountInformationCards
import net.metalbrain.paysmart.ui.home.card.HomeBalanceSummaryCard
import net.metalbrain.paysmart.ui.home.card.RewardEarnedSummaryCard
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeNotificationUiState
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeContent(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onCreateInvoiceClick: () -> Unit,
    onSendMoneyClick: () -> Unit,
    onReceiveMoneyClick: () -> Unit,
    onBalanceCardClick: () -> Unit,
    onRewardCardClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    onViewRatesClick: () -> Unit,
    onViewAllLimitsClick: () -> Unit,
    localSettings: LocalSecuritySettingsModel?,
    displayName: String,
    transactions: List<Transaction>,
    transactionSearchQuery: String,
    isTransactionSearchActive: Boolean,
    availableTransactionProviders: List<HomeTransactionProviderFilter>,
    selectedTransactionProviders: Set<HomeTransactionProviderFilter>,
    notification: HomeNotificationUiState,
    balanceSnapshot: HomeBalanceSnapshot,
    rewardEarned: RewardEarnedSnapshot,
    countryIso2: String,
    countryFlagEmoji: String,
    countryCurrencyCode: String,
    capabilities: List<CapabilityItem>,
    exchangeRateSnapshot: HomeExchangeRateSnapshot,
    isBalanceVisible: Boolean,
    onTransactionSearchQueryChange: (String) -> Unit,
    onTransactionProviderToggle: (HomeTransactionProviderFilter) -> Unit,
    onNotificationPrimaryAction: () -> Unit,
    onToggleBalanceVisibility: () -> Unit
) {
    val availableServices = resolveAvailableServices(
        capabilities = capabilities,
        localSettings = localSettings,
        createInvoiceTitle = stringResource(R.string.home_service_create_invoice),
        onCreateInvoiceClick = onCreateInvoiceClick,
        onSendMoneyClick = onSendMoneyClick,
        onReceiveMoneyClick = onReceiveMoneyClick,
        onAddMoneyClick = onAddMoneyClick
    )
    val setupSecurity = localSettings?.takeIf { settings ->
        !settings.hasCompletedEmailVerification ||
            !settings.hasCompletedAddress ||
            !settings.hasCompletedIdentity
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Dimens.mediumScreenPadding,
            end = Dimens.mediumScreenPadding,
            bottom = Dimens.lg
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        item {
            HomeTopBarContainer(
                onProfileClick = onProfileClick,
                onReferralClick = onReferralClick,
                displayName = displayName,
                countryIso2 = countryIso2,
                countryCurrencyCode = countryCurrencyCode,
                transactionSearchQuery = transactionSearchQuery,
                availableTransactionProviders = availableTransactionProviders,
                selectedTransactionProviders = selectedTransactionProviders,
                notification = notification,
                onTransactionSearchQueryChange = onTransactionSearchQueryChange,
                onTransactionProviderToggle = onTransactionProviderToggle,
                onNotificationPrimaryAction = onNotificationPrimaryAction
            )
        }

        item {
            AccountsHeader(
                isBalanceVisible = isBalanceVisible,
                onToggleVisibility = onToggleBalanceVisibility
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                HomeBalanceSummaryCard(
                    isBalanceVisible = isBalanceVisible,
                    snapshot = balanceSnapshot,
                    modifier = Modifier.weight(1f),
                    onClick = onBalanceCardClick
                )

                RewardEarnedSummaryCard(
                    isBalanceVisible = isBalanceVisible,
                    snapshot = rewardEarned,
                    modifier = Modifier.weight(1f),
                    onClick = onRewardCardClick
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSendMoneyClick,
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.send_money),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = MaterialTheme.colorScheme.primary
                )

                PrimaryButton(
                    text = stringResource(id = R.string.add_money),
                    onClick = onAddMoneyClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (setupSecurity != null) {
            item {
                ProfileCompletionCard(
                    security = setupSecurity,
                    onVerifyEmailClick = onVerifyEmailClick,
                    onAddAddressClick = onAddAddressClick,
                    onVerifyIdentityClick = onVerifyIdentityClick
                )
            }
        }

        item {
            HomeSectionHeader(
                title = stringResource(R.string.home_services_title)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.xs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                items(
                    items = availableServices,
                    key = { service: HomeServiceTile -> service.id }
                ) { service: HomeServiceTile ->
                    ServiceItemCard(service = service)
                }
            }
        }

        homeRecentTransactionsSection(
            transactions = transactions,
            isSearchActive = isTransactionSearchActive,
            onSeeAllClick = onTransactionsClick,
            onAddMoneyClick = onAddMoneyClick,
            onTransactionClick = onTransactionClick
        )

        item {
            HomeSectionHeader(
                title = stringResource(R.string.home_account_information_title)
            )
        }

        item {
            AccountInformationCards(
                localSettings = localSettings,
                countryFlagEmoji = countryFlagEmoji,
                countryCurrencyCode = countryCurrencyCode,
                exchangeRateSnapshot = exchangeRateSnapshot,
                onViewRatesClick = onViewRatesClick,
                onViewAllLimitsClick = onViewAllLimitsClick
            )
        }
    }
}

private fun resolveAvailableServices(
    capabilities: List<CapabilityItem>,
    localSettings: LocalSecuritySettingsModel?,
    createInvoiceTitle: String,
    onCreateInvoiceClick: () -> Unit,
    onSendMoneyClick: () -> Unit,
    onReceiveMoneyClick: () -> Unit,
    onAddMoneyClick: () -> Unit
): List<HomeServiceTile> {
    val sendAllowed = FeatureAccessPolicy.evaluate(
        feature = FeatureKey.SEND_MONEY,
        settings = localSettings
    ).isAllowed
    val addMoneyAllowed = FeatureAccessPolicy.evaluate(
        feature = FeatureKey.ADD_MONEY,
        settings = localSettings
    ).isAllowed
    val receiveMoneyAllowed = FeatureAccessPolicy.evaluate(
        feature = FeatureKey.RECEIVE_MONEY,
        settings = localSettings
    ).isAllowed

    val tiles = mutableListOf(
        HomeServiceTile(
            id = FeatureKey.CREATE_INVOICE.id,
            title = createInvoiceTitle,
            icon = Icons.Filled.Description,
            onClick = onCreateInvoiceClick
        )
    )

    capabilities.filter { item ->
        when (item.key) {
            CapabilityKey.SEND_INTERNATIONAL -> sendAllowed
            CapabilityKey.RECEIVE_MONEY -> receiveMoneyAllowed
            CapabilityKey.CARD_SPEND_ABROAD,
            CapabilityKey.HOLD_AND_CONVERT,
            CapabilityKey.EARN_RETURN -> addMoneyAllowed
        }
    }.forEach { item ->
        tiles += item.toHomeServiceTile(
            onClick = when (item.key) {
                CapabilityKey.SEND_INTERNATIONAL -> onSendMoneyClick
                CapabilityKey.RECEIVE_MONEY -> onReceiveMoneyClick
                CapabilityKey.CARD_SPEND_ABROAD,
                CapabilityKey.HOLD_AND_CONVERT,
                CapabilityKey.EARN_RETURN -> onAddMoneyClick
            }
        )
    }

    return tiles
}

private fun CapabilityItem.toHomeServiceTile(onClick: () -> Unit): HomeServiceTile {
    return HomeServiceTile(
        id = key.name,
        title = title,
        icon = key.icon(),
        onClick = onClick
    )
}

private fun CapabilityKey.icon(): ImageVector {
    return when (this) {
        CapabilityKey.SEND_INTERNATIONAL -> Icons.Filled.Public
        CapabilityKey.CARD_SPEND_ABROAD -> Icons.Filled.CreditCard
        CapabilityKey.HOLD_AND_CONVERT -> Icons.Filled.SwapHoriz
        CapabilityKey.RECEIVE_MONEY -> Icons.Filled.AccountBalance
        CapabilityKey.EARN_RETURN -> Icons.AutoMirrored.Filled.TrendingUp
    }
}

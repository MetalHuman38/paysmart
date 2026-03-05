package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.components.AccountsHeader
import net.metalbrain.paysmart.core.features.account.profile.card.ProfileCompletionCard
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.transactions.components.TransactionItem
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.home.card.HomeBalanceSummaryCard
import net.metalbrain.paysmart.ui.home.card.RewardEarnedSummaryCard
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeContent(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onSendMoneyClick: () -> Unit,
    onBalanceCardClick: () -> Unit,
    onRewardCardClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    onViewRatesClick: () -> Unit,
    onViewAllLimitsClick: () -> Unit,
    localSettings: LocalSecuritySettingsModel?,
    transactions: List<Transaction>,
    balanceSnapshot: HomeBalanceSnapshot,
    rewardEarned: RewardEarnedSnapshot,
    countryFlagEmoji: String,
    countryCurrencyCode: String,
    capabilities: List<CapabilityItem>,
    exchangeRateSnapshot: HomeExchangeRateSnapshot,
    isBalanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit
) {
    val availableServices = remember(capabilities, localSettings) {
        resolveAvailableServices(
            capabilities = capabilities,
            localSettings = localSettings
        )
    }
    val setupSecurity = localSettings?.takeIf { settings ->
        !settings.hasCompletedEmailVerification ||
            !settings.hasCompletedAddress ||
            !settings.hasCompletedIdentity
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = Dimens.mediumScreenPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HomeTopBarContainer(
                onProfileClick = onProfileClick,
                onReferralClick = onReferralClick
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSendMoneyClick,
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.send_money),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
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
            Text(
                text = stringResource(R.string.home_services_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            if (availableServices.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_services_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(
                        items = availableServices,
                        key = { service: CapabilityItem -> service.key.name + service.title }
                    ) { service: CapabilityItem ->
                        ServiceItemCard(service = service)
                    }
                }
            }
        }

        item {
            HomeSectionHeader(
                title = stringResource(id = R.string.transactions_title),
                actionLabel = stringResource(id = R.string.see_all),
                onActionClick = onTransactionsClick
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyTransactionsBlock(onAddMoneyClick = onAddMoneyClick)
            }
        } else {
            items(transactions, key = { it.id }) { transaction ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    TransactionItem(transaction = transaction)
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }



        item {
            Spacer(modifier = Modifier.padding(bottom = 20.dp))
            Text(
                text = stringResource(R.string.home_account_information_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
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
            Spacer(modifier = Modifier.padding(bottom = 20.dp))
        }
    }
}

private fun resolveAvailableServices(
    capabilities: List<CapabilityItem>,
    localSettings: LocalSecuritySettingsModel?
): List<CapabilityItem> {
    if (capabilities.isEmpty()) return emptyList()

    val sendAllowed = FeatureAccessPolicy.evaluate(
        feature = FeatureKey.SEND_MONEY,
        settings = localSettings
    ).isAllowed
    val addMoneyAllowed = FeatureAccessPolicy.evaluate(
        feature = FeatureKey.ADD_MONEY,
        settings = localSettings
    ).isAllowed

    return capabilities.filter { item ->
        when (item.key) {
            CapabilityKey.SEND_INTERNATIONAL -> sendAllowed
            CapabilityKey.CARD_SPEND_ABROAD,
            CapabilityKey.HOLD_AND_CONVERT,
            CapabilityKey.RECEIVE_MONEY,
            CapabilityKey.EARN_RETURN -> addMoneyAllowed
        }
    }
}

package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.account.components.AccountsHeader
import net.metalbrain.paysmart.ui.home.card.EmptyActivityCard
import net.metalbrain.paysmart.ui.home.card.HomeBalanceSummaryCard
import net.metalbrain.paysmart.ui.home.card.RewardEarnedSummaryCard
import net.metalbrain.paysmart.ui.home.data.HomeQuickAction
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import net.metalbrain.paysmart.ui.home.extensions.ProfileCompletionCard
import net.metalbrain.paysmart.ui.transactions.components.TransactionItem
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeContent(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onBalanceCardClick: () -> Unit,
    onRewardCardClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    localSettings: LocalSecuritySettingsModel?,
    transactions: List<Transaction>,
    balanceSnapshot: HomeBalanceSnapshot,
    rewardEarned: RewardEarnedSnapshot,
) {
    val showBalance = rememberSaveable { mutableStateOf(true) }
    val showCompletionCard = localSettings?.let { settings ->
        !settings.hasCompletedEmailVerification ||
            !settings.hasCompletedAddress ||
            !settings.hasCompletedIdentity
    } ?: false

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.mediumScreenPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            HomeTopBarContainer(
                onProfileClick = onProfileClick,
                onReferralClick = onReferralClick
            )
        }

        item {
            AccountsHeader(
                isBalanceVisible = showBalance.value,
                onToggleVisibility = { showBalance.value = !showBalance.value }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeBalanceSummaryCard(
                    isBalanceVisible = showBalance.value,
                    snapshot = balanceSnapshot,
                    modifier = Modifier.weight(1f),
                    onClick = onBalanceCardClick
                )

                RewardEarnedSummaryCard(
                    isBalanceVisible = showBalance.value,
                    snapshot = rewardEarned,
                    modifier = Modifier.weight(1f),
                    onClick = onRewardCardClick
                )
            }
        }

        item {
            HomeQuickActions(
                actions = listOf(
                    HomeQuickAction("Transactions", Icons.Filled.SwapHoriz, onTransactionsClick),
                    HomeQuickAction("Profile", Icons.Filled.Person, onProfileClick),
                    HomeQuickAction("Re-auth", Icons.Filled.Lock, onSecurityClick),
                    HomeQuickAction("Link", Icons.Filled.Link, onLinkAccountClick)
                )
            )
        }

        if (localSettings != null && showCompletionCard) {
            item {
                ProfileCompletionCard(
                    security = localSettings,
                    onVerifyEmailClick = onVerifyEmailClick,
                    onAddAddressClick = onAddAddressClick,
                    onVerifyIdentityClick = onVerifyIdentityClick
                )
            }
        }

        item {
            HomeSectionHeader(
                title = "Recent activity",
                actionLabel = "See all",
                onActionClick = onTransactionsClick
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyActivityCard()
            }
        } else {
            items(transactions, key = { it.id }) { transaction ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransactionItem(transaction = transaction)
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

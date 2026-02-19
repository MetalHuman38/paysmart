package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.account.AccountsHeader
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.extensions.ProfileCompletionCard
import net.metalbrain.paysmart.ui.transactions.components.TransactionItem
import net.metalbrain.paysmart.ui.theme.Dimens
import java.util.Locale

@Composable
fun HomeContent(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    localSettings: LocalSecuritySettingsModel?,
    transactions: List<Transaction>,
    balanceSnapshot: HomeBalanceSnapshot,
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
            HomeBalanceSummaryCard(
                isBalanceVisible = showBalance.value,
                snapshot = balanceSnapshot
            )
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

private data class HomeQuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun HomeBalanceSummaryCard(
    isBalanceVisible: Boolean,
    snapshot: HomeBalanceSnapshot,
) {
    val primaryCurrency = snapshot.primaryCurrencyCode()
    val primaryAmount = snapshot.balancesByCurrency[primaryCurrency] ?: 0.0
    val walletBreakdown = snapshot.walletBreakdownLabel()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = maskedValue(
                    isBalanceVisible,
                    formatCurrencyAmount(primaryCurrency, primaryAmount)
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = maskedValue(
                    isBalanceVisible,
                    walletBreakdown
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (snapshot.rewardsPoints > 0.0) {
                Text(
                    text = maskedValue(
                        isBalanceVisible,
                        "Rewards ${formatAmount(snapshot.rewardsPoints)} pts"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun HomeQuickActions(
    actions: List<HomeQuickAction>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = action.onClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "No recent transactions yet.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun maskedValue(isVisible: Boolean, value: String): String {
    return if (isVisible) value else "******"
}

private fun HomeBalanceSnapshot.primaryCurrencyCode(): String {
    if (balancesByCurrency.isEmpty()) {
        return "GBP"
    }

    if (balancesByCurrency.containsKey("GBP")) {
        return "GBP"
    }

    return balancesByCurrency.keys.minOf { it }
}

private fun HomeBalanceSnapshot.walletBreakdownLabel(): String {
    if (balancesByCurrency.isEmpty()) {
        return "No wallet data yet"
    }

    return balancesByCurrency
        .toList()
        .sortedBy { it.first }
        .take(2)
        .joinToString(" | ") { (currency, amount) ->
            "$currency wallet ${formatAmount(amount)}"
        }
}

private fun formatCurrencyAmount(currencyCode: String, amount: Double): String {
    return "$currencyCode ${formatAmount(amount)}"
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}

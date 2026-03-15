package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.support.balanceAmountForCurrency
import net.metalbrain.paysmart.ui.home.support.resolvePrimaryBalanceCurrency
import net.metalbrain.paysmart.ui.theme.HomeCardTokens
import net.metalbrain.paysmart.ui.theme.Dimens
import java.util.Locale

@Composable
fun HomeBalanceSummaryCard(
    isBalanceVisible: Boolean,
    snapshot: HomeBalanceSnapshot,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val primaryCurrency = snapshot.primaryCurrencyCode()
    val primaryAmount = snapshot.balancesByCurrency.balanceAmountForCurrency(primaryCurrency)
    val walletBreakdown = snapshot.walletBreakdownLabel(
        noDataLabel = stringResource(id = R.string.home_wallet_no_data),
        walletLabel = stringResource(id = R.string.home_wallet_label)
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(HomeCardTokens.summaryCardHeight),
        shape = HomeCardTokens.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.defaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
                        )
                    )
                )
                .padding(HomeCardTokens.contentPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                Text(
                    text = stringResource(id = R.string.home_total_balance_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Text(
                    text = maskedValue(
                        isBalanceVisible,
                        stringResource(
                            id = R.string.home_currency_amount,
                            primaryCurrency,
                            formatAmount(primaryAmount)
                        )
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = maskedValue(isBalanceVisible, walletBreakdown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun HomeBalanceSnapshot.primaryCurrencyCode(): String {
    return resolvePrimaryBalanceCurrency(
        balancesByCurrency = balancesByCurrency,
        preferredCurrencyCode = preferredCurrencyCode
    )
}

private fun HomeBalanceSnapshot.walletBreakdownLabel(
    noDataLabel: String,
    walletLabel: String
): String {
    if (balancesByCurrency.isEmpty()) {
        return noDataLabel
    }

    return balancesByCurrency
        .toList()
        .sortedBy { it.first }
        .take(2)
        .joinToString(" | ") { (currency, amount) ->
            "$currency $walletLabel ${formatAmount(amount)}"
        }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}





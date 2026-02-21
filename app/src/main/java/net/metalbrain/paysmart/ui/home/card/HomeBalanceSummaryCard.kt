package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import java.util.Locale

@Composable
fun HomeBalanceSummaryCard(
    isBalanceVisible: Boolean,
    snapshot: HomeBalanceSnapshot,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val primaryCurrency = snapshot.primaryCurrencyCode()
    val primaryAmount = snapshot.balancesByCurrency[primaryCurrency] ?: 0.0
    val walletBreakdown = snapshot.walletBreakdownLabel()

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(186.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Total balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
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
            }

            Text(
                text = maskedValue(
                    isBalanceVisible,
                    walletBreakdown
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
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

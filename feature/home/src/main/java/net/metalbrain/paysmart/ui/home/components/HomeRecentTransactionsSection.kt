package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.feature.home.R
import net.metalbrain.paysmart.core.features.transactions.components.TransactionItem
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

fun LazyListScope.homeRecentTransactionsSection(
    transactions: List<Transaction>,
    isSearchActive: Boolean,
    onSeeAllClick: () -> Unit,
    launchInterest: LaunchInterest,
    onCreateInvoiceClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    item {
        HomeSectionHeader(
            title = stringResource(id = R.string.transactions_title),
            actionLabel = stringResource(id = R.string.see_all),
            onActionClick = onSeeAllClick
        )
    }

    if (transactions.isEmpty() && isSearchActive) {
        item {
            HomeTransactionSearchEmptyState()
        }
    } else if (transactions.isEmpty()) {
        item {
            EmptyTransactionsBlock(
                launchInterest = launchInterest,
                onAddMoneyClick = onAddMoneyClick,
                onCreateInvoiceClick = onCreateInvoiceClick
            )
        }
    } else {
        items(transactions, key = { it.id }) { transaction ->
            Column(modifier = Modifier.fillMaxWidth()) {
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = Dimens.smallSpacing),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeTransactionSearchEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.home_transaction_search_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.home_transaction_search_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.AccountStatementViewModel

@Composable
fun AccountStatementRoute(
    viewModel: AccountStatementViewModel,
    onBack: () -> Unit
) {
    val transactions = viewModel.transactions.collectAsLazyPagingItems()

    AccountStatementScreen(
        transactions = transactions,
        onBack = onBack
    )
}

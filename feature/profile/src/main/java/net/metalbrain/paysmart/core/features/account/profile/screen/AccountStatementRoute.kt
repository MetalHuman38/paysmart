package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.AccountStatementViewModel

@Composable
fun AccountStatementRoute(
    viewModel: AccountStatementViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    AccountStatementScreen(
        state = state,
        onBack = onBack,
        onCurrencySelected = viewModel::onCurrencySelected,
        onStartDateSelected = viewModel::onStartDateSelected,
        onEndDateSelected = viewModel::onEndDateSelected,
        onFormatSelected = viewModel::onFormatSelected
    )
}

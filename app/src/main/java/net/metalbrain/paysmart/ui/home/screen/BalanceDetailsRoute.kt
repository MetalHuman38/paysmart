package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.ui.home.viewmodel.BalanceDetailsViewModel

@Composable
fun BalanceDetailsRoute(
    viewModel: BalanceDetailsViewModel,
    initialTab: Screen.BalanceDetails.Tab,
    onBack: () -> Unit,
    onViewAccountLimitsClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onConvertClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    BalanceDetailsScreen(
        state = state,
        initialTab = initialTab,
        onBack = onBack,
        onViewAccountLimitsClick = onViewAccountLimitsClick,
        onSendClick = onSendClick,
        onAddClick = onAddClick,
        onWithdrawClick = onWithdrawClick,
        onConvertClick = onConvertClick,
        onTransactionClick = onTransactionClick
    )
}

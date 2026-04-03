package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.viewmodel.RewardDetailsViewModel

@Composable
fun RewardDetailsRoute(
    viewModel: RewardDetailsViewModel,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    RewardDetailsScreen(
        state = state,
        onBack = onBack,
        onHelpClick = onHelpClick,
        onTransactionClick = onTransactionClick
    )
}

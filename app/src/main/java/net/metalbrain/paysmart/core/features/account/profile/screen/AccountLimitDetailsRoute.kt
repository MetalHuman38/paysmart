package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.capabilities.viewmodel.AccountLimitDetailsViewModel

@Composable
fun AccountLimitDetailsRoute(
    onBack: () -> Unit,
    onHelp: () -> Unit,
    viewModel: AccountLimitDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AccountLimitDetailsScreen(
        state = state,
        onBack = onBack,
        onHelp = onHelp,
        onTabSelected = viewModel::onTabSelected
    )
}

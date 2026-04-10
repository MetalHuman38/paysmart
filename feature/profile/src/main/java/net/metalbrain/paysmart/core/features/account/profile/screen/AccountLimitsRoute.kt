package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.capabilities.viewmodel.AccountLimitsListViewModel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@Composable
fun AccountLimitsRoute(
    settings: LocalSecuritySettingsModel?,
    onBack: () -> Unit,
    onAccountClick: (String) -> Unit,
    viewModel: AccountLimitsListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AccountLimitsScreen(
        state = state,
        settings = settings,
        onBack = onBack,
        onAccountClick = onAccountClick
    )
}

package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.creation.components.AccountCreationScaffold
import net.metalbrain.paysmart.core.features.account.creation.components.PostOtpCapabilitiesContent
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.PostOtpCapabilitiesViewModel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun PostOtpCapabilitiesScreen(
    countryIso2: String,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: PostOtpCapabilitiesViewModel = hiltViewModel()
) {
    LaunchedEffect(countryIso2) {
        viewModel.bindCountry(countryIso2)
    }
    val uiState by viewModel.uiState.collectAsState()

    AccountCreationScaffold(onBack = onBack) { innerPadding ->
        PostOtpCapabilitiesContent(
            profile = uiState.profile,
            selectedInterest = uiState.selectedInterest,
            isPersistingSelection = uiState.isPersistingSelection,
            onInterestSelected = viewModel::selectInterest,
            onNext = {
                viewModel.persistSelection(onPersisted = onNext)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.space6)
        )
    }
}

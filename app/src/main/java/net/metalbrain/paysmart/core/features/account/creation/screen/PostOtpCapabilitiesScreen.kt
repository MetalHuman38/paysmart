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

/**
 * Composable that represents the Post-OTP Capabilities screen in the account creation flow.
 * This screen allows the user to view or select interests/capabilities based on their country
 * after the OTP verification process.
 *
 * @param countryIso2 The ISO 3166-1 alpha-2 country code used to fetch country-specific capabilities.
 * @param onNext Callback triggered when the user successfully persists their selection and moves forward.
 * @param onBack Callback triggered when the user navigates back to the previous screen.
 * @param viewModel The [PostOtpCapabilitiesViewModel] that manages the screen's state and logic.
 */
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

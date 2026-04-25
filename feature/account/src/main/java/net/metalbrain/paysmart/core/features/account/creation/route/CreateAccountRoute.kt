package net.metalbrain.paysmart.core.features.account.creation.route

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.creation.screen.CreateAccountScreen
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.CreateAccountViewModel

@Composable
fun CreateAccountRoute(
    onVerificationContinue: (dialCode: String, phoneNumber: String, countryIso2: String) -> Unit,
    onGetHelp: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: CreateAccountViewModel = hiltViewModel()
    CreateAccountScreen(
        viewModel = viewModel,
        onVerificationContinue = { countryIso2 ->
            onVerificationContinue(
                viewModel.selectedCountry.value.dialCode,
                viewModel.phoneNumber,
                countryIso2
            )
        },
        onGetHelpClicked = onGetHelp,
        onSignInClicked = onSignIn,
        onBackClicked = onBack,
    )
}

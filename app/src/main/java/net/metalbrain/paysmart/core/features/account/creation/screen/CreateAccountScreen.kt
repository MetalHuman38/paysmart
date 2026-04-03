package net.metalbrain.paysmart.core.features.account.creation.screen

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.components.AccountCreationScaffold
import net.metalbrain.paysmart.core.features.account.creation.components.CreateAccountContent
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.ui.components.PhoneAlreadyRegisteredSheet
import net.metalbrain.paysmart.ui.components.SmallTextButton
import net.metalbrain.paysmart.ui.screens.CountryPickerBottomSheet
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.utils.detectDeviceCountryIso2

/**
 * Composable that represents the account creation screen.
 *
 * This screen allows users to input their phone number, select their country, and accept
 * terms and conditions to initiate the phone verification process.
 *
 * @param viewModel The [CreateAccountViewModel] that manages the state and logic for this screen.
 * @param onVerificationContinue Callback triggered when the phone verification has successfully
 * started, passing the normalized ISO country code.
 * @param onGetHelpClicked Callback triggered when the "Get Help" button is clicked.
 * @param onSignInClicked Callback triggered when the user opts to navigate to the sign-in screen.
 * @param onBackClicked Callback triggered when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onVerificationContinue: (String) -> Unit,
    onGetHelpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onBackClicked: () -> Unit
) {

    var showCountryPicker by remember { mutableStateOf(false) }
    val selectedCountry by viewModel.selectedCountry
    var showAlreadyRegisteredSheet by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val phoneVerificationStartError = stringResource(
        R.string.create_account_phone_verification_error
    )

    LaunchedEffect(Unit) {
        viewModel.autoSelectCountry(detectDeviceCountryIso2(context))
    }

    if (showAlreadyRegisteredSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlreadyRegisteredSheet = false },
            shape = MaterialTheme.shapes.large
        ) {
            PhoneAlreadyRegisteredSheet(
                onDismiss = { showAlreadyRegisteredSheet = false }
            )
        }
    }

    AccountCreationScaffold(
        onBack = onBackClicked,
        topBarAction = {
            SmallTextButton(
                text = stringResource(R.string.get_help),
                onClick = onGetHelpClicked,
            )
        }
    ) { innerPadding ->
        CreateAccountContent(
            selectedCountry = selectedCountry,
            phoneNumber = viewModel.phoneNumber,
            acceptedMarketing = viewModel.acceptedMarketing,
            acceptedTerms = viewModel.acceptedTerms,
            isSubmitting = isSubmitting,
            errorMessage = submissionError,
            isContinueEnabled = viewModel.acceptedTerms && viewModel.isPhoneValid(),
            onFlagClick = { showCountryPicker = true },
            onPhoneNumberChange = { value ->
                submissionError = null
                viewModel.onPhoneNumberChanged(value)
            },
            onToggleMarketing = viewModel::onToggleMarketing,
            onToggleTerms = viewModel::onToggleTerms,
            onContinue = {
                isSubmitting = true
                submissionError = null
                scope.launch {
                    viewModel.startPhoneVerification(
                        activity = activity,
                        onVerificationStarted = {
                            isSubmitting = false
                            onVerificationContinue(normalizeCountryIso2(selectedCountry.isoCode))
                        },
                        onPhoneAlreadyRegistered = {
                            isSubmitting = false
                            showAlreadyRegisteredSheet = true
                        },
                        onError = { error ->
                            isSubmitting = false
                            submissionError = error.message
                                ?.takeIf { it.isNotBlank() }
                                ?: phoneVerificationStartError
                            Log.e("PhoneAuth", error.message ?: "Unknown error")
                        }
                    )
                }
            },
            onSignInClicked = onSignInClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.md, vertical = Dimens.md)
        )
    }

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            onDismiss = { showCountryPicker = false },
            onCountrySelected = { selected ->
                submissionError = null
                viewModel.onCountrySelected(selected)
                showCountryPicker = false
            },
            selectedCountryIso2 = selectedCountry.isoCode
        )
    }

}

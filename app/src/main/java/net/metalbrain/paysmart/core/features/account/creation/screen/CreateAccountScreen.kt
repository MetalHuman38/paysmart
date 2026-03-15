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
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
            isContinueEnabled = viewModel.acceptedTerms && viewModel.isPhoneValid(),
            onFlagClick = { showCountryPicker = true },
            onPhoneNumberChange = viewModel::onPhoneNumberChanged,
            onToggleMarketing = viewModel::onToggleMarketing,
            onToggleTerms = viewModel::onToggleTerms,
            onContinue = {
                isSubmitting = true
                scope.launch {
                    viewModel.startPhoneVerification(
                        activity = activity,
                        onSuccess = {
                            isSubmitting = false
                            onVerificationContinue(normalizeCountryIso2(selectedCountry.isoCode))
                        },
                        onPhoneAlreadyRegistered = {
                            isSubmitting = false
                            showAlreadyRegisteredSheet = true
                        },
                        onError = { error ->
                            isSubmitting = false
                            Log.e("PhoneAuth", error.message ?: "Unknown error")
                        }
                    )
                }
            },
            onSignInClicked = onSignInClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.space6)
        )
    }

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            onDismiss = { showCountryPicker = false },
            onCountrySelected = { selected ->
                viewModel.onCountrySelected(selected)
                showCountryPicker = false
            }
        )
    }

}

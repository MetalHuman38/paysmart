package net.metalbrain.paysmart.ui.screens

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.HaveAnAccount
import net.metalbrain.paysmart.ui.components.PhoneAlreadyRegisteredSheet
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SmallTextButton
import net.metalbrain.paysmart.ui.components.TermsAndPrivacyText
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.CreateAccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onContinue: () -> Unit,
    onBackClicked: () -> Unit
) {

    var showCountryPicker by remember { mutableStateOf(false) }
    val selectedCountry by viewModel.selectedCountry
    var showAlreadyRegisteredSheet by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()


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



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.mediumSpacing)
            .padding(bottom = Dimens.mediumSpacing)
            .padding(horizontal = Dimens.screenPadding)
            .verticalScroll(scrollState)
    ) {

        // 🔙 Back
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onBackClicked() }
            )

            // Space between
            Spacer(modifier = Modifier.weight(1f))

            SmallTextButton(
                text = stringResource(R.string.get_help),
                onClick = onContinue,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.lets_get_started),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_phone_to_signup),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        PhoneNumberInput(
            selectedCountry = selectedCountry,
            phoneNumber = viewModel.phoneNumber,
            onPhoneNumberChange = viewModel::onPhoneNumberChanged,
            onFlagClick = { showCountryPicker = true }
        )

        Spacer(Modifier.height(18.dp))

        // 🧾 Checkbox 1 - Marketing
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Checkbox(
                checked = viewModel.acceptedMarketing,
                onCheckedChange = { viewModel.onToggleMarketing() }
            )
            Text(
                text = stringResource(R.string.marketing),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Spacer
        Spacer(Modifier.height(16.dp))


// 🧾 Checkbox 2 - Terms
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Checkbox(
                checked = viewModel.acceptedTerms,
                onCheckedChange = { viewModel.onToggleTerms() }
            )
            TermsAndPrivacyText(
                onTermsClicked = {},
                onPrivacyClicked = {}
            )
        }

        // Space
        Spacer(modifier = Modifier.height(14.dp))

        // 🔘 Continue
        PrimaryButton(
            text = stringResource(R.string.continue_text),
            onClick = {
                isSubmitting = true
                scope.launch {
                    viewModel.startPhoneVerification(
                        activity = activity,
                        onSuccess = {
                            isSubmitting = false
                            onContinue()
                        },
                        onPhoneAlreadyRegistered = {
                            // 👇 This is the lambda we pass to show the bottom sheet
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
            enabled = viewModel.acceptedTerms && viewModel.isPhoneValid(),
            isLoading = isSubmitting,
            loadingText = "Sending OTP..."
        )

        // Space
        Spacer(modifier = Modifier.height(14.dp))


        // Have an account? Sign In
        HaveAnAccount(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            onSignInClicked = onContinue
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

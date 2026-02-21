package net.metalbrain.paysmart.ui.account.recovery.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.supportedCountries
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.ui.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.screens.CountryPickerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneRecoveryScreen(
    viewModel: ChangePhoneRecoveryViewModel,
    activity: FragmentActivity,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(supportedCountries.first()) }
    var nationalPhoneInput by rememberSaveable { mutableStateOf("") }

    if (state.isSuccess) {
        onSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change phone number") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Verify a new phone number to keep account recovery and sign-in secure.",
                style = MaterialTheme.typography.bodyMedium
            )

            PhoneNumberInput(
                selectedCountry = selectedCountry,
                phoneNumber = nationalPhoneInput,
                onPhoneNumberChange = {
                    val digits = it.filter(Char::isDigit)
                    nationalPhoneInput = digits
                    viewModel.onPhoneNumberChanged("${selectedCountry.dialCode}$digits")
                },
                onFlagClick = {
                    showCountryPicker = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Send OTP",
                onClick = { viewModel.sendCode(activity) },
                enabled = nationalPhoneInput.isNotBlank(),
                isLoading = state.isLoading,
                loadingText = "Sending..."
            )

            if (state.isCodeSent) {
                OutlinedTextField(
                    value = state.otpCode,
                    onValueChange = viewModel::onOtpChanged,
                    label = { Text("OTP code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                PrimaryButton(
                    text = "Confirm phone change",
                    onClick = { viewModel.confirmCode() },
                    enabled = state.otpCode.length >= 6,
                    isLoading = state.isLoading,
                    loadingText = "Verifying..."
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (showCountryPicker) {
                CountryPickerBottomSheet(
                    onDismiss = { showCountryPicker = false },
                    onCountrySelected = { country ->
                        selectedCountry = country
                        viewModel.onPhoneNumberChanged("${country.dialCode}$nationalPhoneInput")
                        showCountryPicker = false
                    }
                )
            }
        }
    }
}

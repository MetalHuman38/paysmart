package net.metalbrain.paysmart.core.features.account.recovery.screen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.matchCountryByInternationalPrefix
import net.metalbrain.paysmart.domain.model.supportedCountries
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import net.metalbrain.paysmart.core.features.account.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.core.features.account.components.CountryPickerBottomSheet
import net.metalbrain.paysmart.utils.detectDeviceCountryIso2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneRecoveryScreen(
    viewModel: ChangePhoneRecoveryViewModel,
    activity: FragmentActivity,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var selectedCountry by remember {
        mutableStateOf(
            supportedCountries.firstOrNull { it.isoCode == DEFAULT_COUNTRY_ISO2 }
                ?: supportedCountries.first()
        )
    }
    var nationalPhoneInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val detectedIso = detectDeviceCountryIso2(context)
        val detected = supportedCountries.firstOrNull {
            it.isoCode.equals(detectedIso, ignoreCase = true)
        }
        if (detected != null && nationalPhoneInput.isBlank()) {
            selectedCountry = detected
            viewModel.onPhoneNumberChanged("${detected.dialCode}$nationalPhoneInput")
        }
    }

    if (state.isSuccess) {
        onSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_phone_recovery_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
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
                text = stringResource(R.string.change_phone_recovery_description),
                style = MaterialTheme.typography.bodyMedium
            )

            PhoneNumberInput(
                selectedCountry = selectedCountry,
                phoneNumber = nationalPhoneInput,
                onPhoneNumberChange = {
                    val matched = matchCountryByInternationalPrefix(it)
                    if (matched != null) {
                        selectedCountry = matched.first
                        nationalPhoneInput = matched.second
                        viewModel.onPhoneNumberChanged("${matched.first.dialCode}${matched.second}")
                    } else {
                        val digits = it.filter(Char::isDigit).take(15)
                        nationalPhoneInput = digits
                        viewModel.onPhoneNumberChanged("${selectedCountry.dialCode}$digits")
                    }
                },
                onFlagClick = {
                    showCountryPicker = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = stringResource(R.string.change_phone_send_otp),
                onClick = { viewModel.sendCode(activity) },
                enabled = nationalPhoneInput.isNotBlank(),
                isLoading = state.isLoading,
                loadingText = stringResource(R.string.change_phone_sending)
            )

            if (state.isCodeSent) {
                OutlinedTextField(
                    value = state.otpCode,
                    onValueChange = viewModel::onOtpChanged,
                    label = { Text(stringResource(R.string.change_phone_otp_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                PrimaryButton(
                    text = stringResource(R.string.change_phone_confirm_action),
                    onClick = { viewModel.confirmCode() },
                    enabled = state.otpCode.length >= 6,
                    isLoading = state.isLoading,
                    loadingText = stringResource(R.string.reauth_verifying)
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
                    },
                    selectedCountryIso2 = selectedCountry.isoCode
                )
            }
        }
    }
}

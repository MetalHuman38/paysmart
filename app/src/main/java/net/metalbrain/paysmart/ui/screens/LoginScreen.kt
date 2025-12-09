package net.metalbrain.paysmart.ui.screens

import CustomTextField
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.DontHaveAnAccount
import net.metalbrain.paysmart.ui.components.LanguageSelector
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SmallTextButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    languageViewModel: LanguageViewModel,
    onContinue: () -> Unit,
    onBackClicked: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onLanguageSelect: () -> Unit
) {
    val selectedCountry by viewModel.selectedCountry
    val phoneNumber by viewModel.phoneNumber
    val password by viewModel.password
    val isPasswordVisible by viewModel.isPasswordVisible

    val currentLang by languageViewModel.currentLanguage.collectAsState()
    var showCountryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = Dimens.screenPadding)
    ) {
        // 🔹 Language Selector (Top-right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.mediumSpacing),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(
                currentLanguage = currentLang,
                onClick = onLanguageSelect
            )
        }

        Spacer(Modifier.height(24.dp))

        // 🔹 Title
        Text(
            text = stringResource(R.string.log_in_to_paySmart),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_phone_to_signup),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 🔹 Phone Field
        PhoneNumberInput(
            selectedCountry = selectedCountry,
            phoneNumber = phoneNumber,
            onPhoneNumberChange = viewModel::onPhoneNumberChanged,
            onFlagClick = { showCountryDialog = true }
        )

        Spacer(Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = viewModel::onPasswordChanged,
            placeholder = stringResource(R.string.enter_your_password),
            modifier = Modifier.fillMaxWidth(),
            isPassword = true
        )


        Spacer(Modifier.height(12.dp))

        // 🔹 Forgot Password
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            SmallTextButton(
                text = stringResource(R.string.recover_your_account),
                onClick = onForgotPassword
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 🔹 Login Button
        PrimaryButton(
            onClick = onContinue,
            text = stringResource(R.string.log_in),
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        // 🔹 Signup Link
        DontHaveAnAccount(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            onSignInClicked = onContinue
        )

        // 🔹 Country Picker Dialog
        if (showCountryDialog) {
            CountryPickerDialog(
                onDismiss = { showCountryDialog = false },
                onCountrySelected = {
                    viewModel.onCountrySelected(it)
                    showCountryDialog = false
                }
            )
        }
    }
}

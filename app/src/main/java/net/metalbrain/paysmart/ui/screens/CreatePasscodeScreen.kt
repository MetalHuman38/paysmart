package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.PasscodeViewModel

@Composable
fun SetPasscodeScreen(
    viewModel: PasscodeViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val passcode = uiState.passcode
    val confirm = uiState.confirmPasscode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding)

    ) {
        Text(stringResource(
            id = R.string.set_up_your_pass_code),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary

        )

        Spacer(Modifier.height(8.dp))
//        Text("This password is stored only on your device and used to authorize transactions.")
        Text(stringResource(
            id = R.string.passcode_use_to_unlock),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary

        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = passcode,
            onValueChange = viewModel::onPasscodeChanged,
            label = { Text("Enter passcode") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = viewModel::onConfirmPasscodeChanged,
            label = { Text("Confirm passcode") },
            singleLine = true,
            isError = confirm.isNotBlank() && passcode != confirm,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = "Save passcode",
            onClick = { viewModel.submitPasscode(onDone) },
            enabled = passcode.length >= 4 && passcode == confirm && !uiState.loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

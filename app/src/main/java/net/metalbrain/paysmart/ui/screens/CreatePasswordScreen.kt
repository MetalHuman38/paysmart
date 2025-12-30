package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import net.metalbrain.paysmart.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.RequirementsList
import net.metalbrain.paysmart.ui.components.StrengthMeter
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.utils.evaluatePassword

@Composable
fun CreateLocalPasswordScreen(
    viewModel: CreatePasswordViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // 🌀 Show animated spinner while loading
    if (uiState.loading) {
        AppLoadingScreen(message = "Saving your password...")
        return
    }

    val pw = uiState.password
    val pw2 = uiState.confirmPassword
    val checks = remember(pw) { evaluatePassword(pw) }
    val allGood = checks.allPassed && pw == pw2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding)

    ){
//
        Text(stringResource(
            id = R.string.create_secure_password),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary

        )

        Spacer(Modifier.height(8.dp))
//        Text("This password is stored only on your device and used to authorize transactions.")
        Text(stringResource(
            id = R.string.enter_secure_password),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary

        )

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pw,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        StrengthMeter(checks)

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pw2,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = { Text("Confirm password") },
            isError = pw2.isNotBlank() && pw != pw2,
            visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        RequirementsList(checks)

        Spacer(Modifier.weight(1f))

        if (!uiState.errorMessage.isNullOrBlank()) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Register",
            onClick = {
                viewModel.submitPassword(onSuccess = onDone)
            },
            enabled = allGood,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

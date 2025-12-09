package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.ui.components.RequirementsList
import net.metalbrain.paysmart.ui.components.StrengthMeter
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.utils.evaluatePassword

@Composable
fun CreateLocalPasswordScreen(
    viewModel: CreatePasswordViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val pw = uiState.password
    val pw2 = uiState.confirmPassword
    val checks = remember(pw) { evaluatePassword(pw) }
    val allGood = checks.allPassed && pw == pw2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp),
    ) {
        Text("Create your password", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(8.dp))
        Text("This password is stored only on your device and used to authorize transactions.")

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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        RequirementsList(checks)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.submitPassword(onSuccess = onDone)
            },
            enabled = allGood && !uiState.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

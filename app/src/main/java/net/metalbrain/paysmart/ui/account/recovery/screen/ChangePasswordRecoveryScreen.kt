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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.components.PasswordInputField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.account.recovery.viewmodel.ChangePasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordRecoveryScreen(
    viewModel: ChangePasswordViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    if (state.success) {
        onSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change password") },
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
                text = "Use your current local password to set a new one.",
                style = MaterialTheme.typography.bodyMedium
            )

            PasswordInputField(
                value = state.oldPassword,
                onValueChange = viewModel::onOldPasswordChanged,
                placeholder = "Current password",
                isPassword = true
            )

            PasswordInputField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                placeholder = "New password",
                isPassword = true
            )

            PasswordInputField(
                value = state.confirmNewPassword,
                onValueChange = viewModel::onConfirmNewPasswordChanged,
                placeholder = "Confirm new password",
                isPassword = true
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = "Update password",
                onClick = { viewModel.submitPasswordChange() },
                enabled = state.oldPassword.isNotBlank() &&
                    state.newPassword.length >= 6 &&
                    state.confirmNewPassword.isNotBlank(),
                isLoading = state.loading,
                loadingText = "Updating...",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

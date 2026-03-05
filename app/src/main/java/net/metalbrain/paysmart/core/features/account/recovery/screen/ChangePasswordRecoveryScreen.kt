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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePasswordViewModel
import net.metalbrain.paysmart.ui.components.PasswordInputField
import net.metalbrain.paysmart.ui.components.PrimaryButton

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
                title = { Text(stringResource(R.string.change_password_recovery_title)) },
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
                text = stringResource(R.string.change_password_recovery_description),
                style = MaterialTheme.typography.bodyMedium
            )

            PasswordInputField(
                value = state.oldPassword,
                onValueChange = viewModel::onOldPasswordChanged,
                placeholder = stringResource(R.string.change_password_current_label),
                isPassword = true
            )

            PasswordInputField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                placeholder = stringResource(R.string.change_password_new_label),
                isPassword = true
            )

            PasswordInputField(
                value = state.confirmNewPassword,
                onValueChange = viewModel::onConfirmNewPasswordChanged,
                placeholder = stringResource(R.string.change_password_confirm_label),
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
                text = stringResource(R.string.change_password_update_action),
                onClick = { viewModel.submitPasswordChange() },
                enabled = state.oldPassword.isNotBlank() &&
                    state.newPassword.length >= 6 &&
                    state.confirmNewPassword.isNotBlank(),
                isLoading = state.loading,
                loadingText = stringResource(R.string.change_password_updating),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

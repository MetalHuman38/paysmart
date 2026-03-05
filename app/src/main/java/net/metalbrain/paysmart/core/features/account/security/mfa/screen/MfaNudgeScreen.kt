package net.metalbrain.paysmart.core.features.account.security.mfa.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaNudgeScreen(
    viewModel: MfaNudgeViewModel,
    onBack: () -> Unit,
    onPrimaryAction: (hasVerifiedEmail: Boolean) -> Unit,
    onSkip: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mfa_prompt_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mfa_prompt_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.mfa_prompt_card_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!uiState.hasVerifiedEmail) {
                        Text(
                            text = stringResource(R.string.mfa_prompt_email_required_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (uiState.hasEnrolledFactor) {
                        Text(
                            text = stringResource(R.string.mfa_prompt_already_enabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.destinationHint?.let {
                        Text(
                            text = stringResource(R.string.mfa_prompt_destination_hint, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.hasSentCode && !uiState.hasEnrolledFactor) {
                        OutlinedTextField(
                            value = uiState.verificationCode,
                            onValueChange = viewModel::onVerificationCodeChanged,
                            label = { Text(stringResource(R.string.mfa_prompt_code_label)) },
                            placeholder = { Text(stringResource(R.string.mfa_prompt_code_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextButton(
                            onClick = {
                                if (activity != null) {
                                    viewModel.resendCode(activity)
                                }
                            },
                            enabled = !uiState.isSendingCode &&
                                !uiState.isStartingEnrollment &&
                                !uiState.isVerifyingCode
                        ) {
                            Text(stringResource(R.string.mfa_prompt_resend_action))
                        }
                    }
                }
            }

            uiState.infoMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        uiState.hasEnrolledFactor -> {
                            onPrimaryAction(true)
                        }

                        !uiState.hasVerifiedEmail -> {
                            viewModel.markPromptHandledForSetup()
                            onPrimaryAction(false)
                        }

                        !uiState.hasSentCode -> {
                            if (activity != null) {
                                viewModel.startSessionAndSendCode(activity)
                            }
                        }

                        else -> {
                            viewModel.verifyCodeAndEnroll()
                        }
                    }
                },
                enabled = !uiState.loading &&
                    !uiState.isStartingEnrollment &&
                    !uiState.isSendingCode &&
                    !uiState.isVerifyingCode &&
                    (!uiState.hasSentCode || uiState.verificationCode.length == 6 || uiState.hasEnrolledFactor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        when {
                            uiState.hasEnrolledFactor -> R.string.mfa_prompt_continue_action
                            !uiState.hasVerifiedEmail -> R.string.mfa_prompt_primary_verify_email_action
                            !uiState.hasSentCode -> R.string.mfa_prompt_send_code_action
                            else -> R.string.mfa_prompt_verify_code_action
                        }
                    )
                )
            }

            if (!uiState.hasEnrolledFactor && uiState.hasVerifiedEmail) {
                Text(
                    text = stringResource(
                        if (uiState.hasSentCode) {
                            R.string.mfa_prompt_code_help
                        } else {
                            R.string.mfa_prompt_primary_action
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.skipInitialPrompt()
                    onSkip()
                },
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.mfa_prompt_skip_action))
            }
        }
    }
}

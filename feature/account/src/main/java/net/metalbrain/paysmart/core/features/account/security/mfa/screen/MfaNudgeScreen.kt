package net.metalbrain.paysmart.core.features.account.security.mfa.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SecondaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun MfaNudgeScreen(
    viewModel: MfaNudgeViewModel,
    onBack: () -> Unit,
    onPrimaryAction: (hasVerifiedEmail: Boolean) -> Unit,
    onBlockedAction: () -> Unit,
    onSkip: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current
    val scrollState = rememberScrollState()
    val isBusy = uiState.isStartingEnrollment || uiState.isSendingCode || uiState.isVerifyingCode
    val showBlockedAction = !uiState.hasEnrolledFactor &&
        !uiState.supportsEnrollment &&
        !uiState.blockedActionLabel.isNullOrBlank()
    val shouldUseBlockedActionAsPrimary = showBlockedAction && uiState.hasVerifiedEmail
    val primaryActionText = stringResource(
        when {
            shouldUseBlockedActionAsPrimary -> R.string.mfa_prompt_continue_action
            uiState.hasEnrolledFactor -> R.string.mfa_prompt_continue_action
            !uiState.hasVerifiedEmail -> R.string.mfa_prompt_primary_verify_email_action
            !uiState.hasSentCode -> R.string.mfa_prompt_send_code_action
            else -> R.string.mfa_prompt_verify_code_action
        }
    )
    val resolvedPrimaryActionText = if (shouldUseBlockedActionAsPrimary) {
        uiState.blockedActionLabel.orEmpty()
    } else {
        primaryActionText
    }
    val helperText = if (!uiState.hasEnrolledFactor && uiState.hasVerifiedEmail) {
        stringResource(
            if (uiState.hasSentCode) {
                R.string.mfa_prompt_code_help
            } else {
                R.string.mfa_prompt_primary_action
            }
        )
    } else {
        null
    }
    val canTriggerPrimaryAction = when {
        shouldUseBlockedActionAsPrimary -> true
        uiState.hasEnrolledFactor -> true
        !uiState.hasVerifiedEmail -> true
        !uiState.supportsEnrollment -> false
        !uiState.hasSentCode -> true
        else -> uiState.verificationCode.length == 6
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                AuthScreenTitle(text = stringResource(R.string.mfa_prompt_title))
                AuthScreenSubtitle(text = stringResource(R.string.mfa_prompt_card_description))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Text(
                        text = stringResource(R.string.mfa_prompt_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!uiState.hasVerifiedEmail) {
                        Text(
                            text = stringResource(R.string.mfa_prompt_email_required_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    uiState.enrollmentBlockMessage?.let {
                        Text(
                            text = it,
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
                            enabled = !isBusy
                        ) {
                            Text(
                                text = stringResource(R.string.mfa_prompt_resend_action),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                PrimaryButton(
                    text = resolvedPrimaryActionText,
                    onClick = {
                        when {
                            shouldUseBlockedActionAsPrimary -> {
                                onBlockedAction()
                            }

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
                        !isBusy &&
                        canTriggerPrimaryAction,
                    isLoading = isBusy,
                    loadingText = stringResource(R.string.common_processing)
                )

                if (showBlockedAction && !shouldUseBlockedActionAsPrimary) {
                    SecondaryButton(
                        text = uiState.blockedActionLabel.orEmpty(),
                        onClick = onBlockedAction,
                        enabled = !uiState.loading
                    )
                }

                helperText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SecondaryButton(
                    text = stringResource(R.string.mfa_prompt_skip_action),
                    onClick = {
                        viewModel.skipInitialPrompt()
                        onSkip()
                    },
                    enabled = !uiState.loading
                )
            }
        }
    }
}

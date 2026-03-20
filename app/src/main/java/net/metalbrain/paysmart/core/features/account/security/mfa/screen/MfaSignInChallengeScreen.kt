package net.metalbrain.paysmart.core.features.account.security.mfa.screen

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaSignInFactorOption
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaSignInViewModel
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SecondaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun MfaSignInChallengeScreen(
    viewModel: MfaSignInViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current
    val scrollState = rememberScrollState()
    val isBusy = uiState.isSendingCode || uiState.isVerifyingCode
    val canVerify = uiState.verificationCode.length == 6

    BackHandler {
        viewModel.clearPendingChallenge()
        onBack()
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onSuccess()
        }
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
                IconButton(
                    onClick = {
                        viewModel.clearPendingChallenge()
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                AuthScreenTitle(text = stringResource(R.string.mfa_sign_in_title))
                AuthScreenSubtitle(text = stringResource(R.string.mfa_sign_in_description))
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
                        text = stringResource(R.string.mfa_sign_in_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.factors.size > 1) {
                        Text(
                            text = stringResource(R.string.mfa_sign_in_factor_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                            uiState.factors.forEach { factor ->
                                MfaFactorOptionRow(
                                    factor = factor,
                                    selected = factor.factorUid == uiState.selectedFactorUid,
                                    onSelected = { viewModel.onFactorSelected(factor.factorUid) }
                                )
                            }
                        }
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

                    if (uiState.hasSentCode) {
                        OutlinedTextField(
                            value = uiState.verificationCode,
                            onValueChange = viewModel::onVerificationCodeChanged,
                            label = { Text(stringResource(R.string.mfa_prompt_code_label)) },
                            placeholder = { Text(stringResource(R.string.mfa_prompt_code_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = stringResource(R.string.mfa_prompt_code_help),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = {
                                if (activity != null) {
                                    viewModel.resendCode(activity)
                                }
                            },
                            enabled = !isBusy && activity != null
                        ) {
                            Text(text = stringResource(R.string.mfa_prompt_resend_action))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                PrimaryButton(
                    text = stringResource(
                        if (uiState.hasSentCode) {
                            R.string.mfa_sign_in_verify_action
                        } else {
                            R.string.mfa_sign_in_send_action
                        }
                    ),
                    onClick = {
                        if (uiState.hasSentCode) {
                            viewModel.verifyCodeAndSignIn()
                        } else if (activity != null) {
                            viewModel.sendCode(activity)
                        }
                    },
                    enabled = !uiState.loading &&
                        !isBusy &&
                        uiState.hasPendingChallenge &&
                        activity != null &&
                        (!uiState.hasSentCode || canVerify),
                    isLoading = isBusy,
                    loadingText = stringResource(R.string.common_processing)
                )

                SecondaryButton(
                    text = stringResource(R.string.mfa_sign_in_back_to_login),
                    onClick = {
                        viewModel.clearPendingChallenge()
                        onBack()
                    },
                    enabled = !isBusy
                )
            }
        }
    }
}

@Composable
private fun MfaFactorOptionRow(
    factor: MfaSignInFactorOption,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected),
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = factor.displayName ?: factor.destinationHint,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!factor.displayName.isNullOrBlank()) {
                    Text(
                        text = factor.destinationHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

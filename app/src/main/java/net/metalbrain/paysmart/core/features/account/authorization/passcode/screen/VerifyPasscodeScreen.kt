package net.metalbrain.paysmart.core.features.account.authorization.passcode.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.authorization.biometric.provider.BiometricHelper
import net.metalbrain.paysmart.core.features.account.authorization.passcode.card.BrandFooter
import net.metalbrain.paysmart.core.features.account.authorization.passcode.card.PasscodeMessageCard
import net.metalbrain.paysmart.core.features.account.authorization.passcode.component.NumberPad
import net.metalbrain.paysmart.core.features.account.authorization.passcode.component.PasscodeIndicatorRow
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel.VerifyPasscodeViewModel
import net.metalbrain.paysmart.utils.shake

@Composable
fun VerifyPasscodeScreen(
    viewModel: VerifyPasscodeViewModel = hiltViewModel(),
    onVerified: () -> Unit,
) {
    val passcode by viewModel.passcode.collectAsState()
    val error by viewModel.error.collectAsState()
    val biometricPrompt by viewModel.biometricPrompt.collectAsState()
    val verified by viewModel.verified.collectAsState()
    val activity = LocalActivity.current as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isLockedOut by viewModel.isLockedOut.collectAsState()
    val shakeTrigger by viewModel.shakeTrigger
    val biometricFailedMessage = stringResource(R.string.verify_passcode_biometric_failed)

    LaunchedEffect(biometricPrompt, activity) {
        if (biometricPrompt) {
            if (activity == null || !BiometricHelper.isBiometricAvailable(activity)) {
                viewModel.onBiometricDismissed()
                scope.launch {
                    snackbarHostState.showSnackbar(biometricFailedMessage)
                }
                return@LaunchedEffect
            }

            BiometricHelper.showPrompt(
                activity = activity,
                title = activity.getString(R.string.biometric_prompt_title),
                subtitle = activity.getString(R.string.biometric_prompt_subtitle),
                onSuccess = {
                    viewModel.onBiometricDismissed()
                    onVerified()
                },
                onError = {
                    viewModel.onBiometricDismissed()
                    scope.launch {
                        snackbarHostState.showSnackbar(biometricFailedMessage)
                    }
                },
                onFailureLimitReached = {
                    viewModel.onBiometricDismissed()
                    scope.launch {
                        snackbarHostState.showSnackbar(biometricFailedMessage)
                    }
                }
            )
        }
    }

    LaunchedEffect(verified) {
        if (verified) {
            viewModel.onVerifiedConsumed()
            onVerified()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    WindowInsets.systemBars.asPaddingValues()
                )
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.enter_your_passcode),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.passcode_use_to_unlock),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.lg, vertical = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PasscodeIndicatorRow(
                                passcode = passcode,
                                isError = error != null,
                                modifier = Modifier.shake(trigger = shakeTrigger)
                            )

                            Text(
                                text = stringResource(R.string.passcode_use_to_unlock),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            if (error != null || isLockedOut) {
                                LaunchedEffect(error) {
                                    if (error != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }

                                PasscodeMessageCard(
                                    message = if (isLockedOut) {
                                        stringResource(R.string.verify_passcode_lockout_message)
                                    } else {
                                        error.orEmpty()
                                    },
                                    isError = true
                                )
                            }

                            PrimaryButton(
                                text = stringResource(R.string.continue_text),
                                onClick = viewModel::submitPasscode,
                                enabled = passcode.length in 4..6 && !isLockedOut
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.xl),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                )
            ) {
                NumberPad(
                    onDigitPressed = { if (!isLockedOut) viewModel.appendDigit(it) },
                    onBackspace = { if (!isLockedOut) viewModel.removeLastDigit() }
                )
            }

            BrandFooter()
        }
    }
}

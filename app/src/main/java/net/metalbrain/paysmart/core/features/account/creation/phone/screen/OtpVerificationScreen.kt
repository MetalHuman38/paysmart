package net.metalbrain.paysmart.core.features.account.creation.phone.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.OTPViewModel
import net.metalbrain.paysmart.ui.components.OtpTextFieldRow
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.screens.loader.LoadingState
import net.metalbrain.paysmart.ui.screens.loader.rememberStabilizedLoading
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OTPViewModel
) {
    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    val isOtpComplete = otpDigits.all { it.length == 1 }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val activity = LocalActivity.current
    val scrollState = rememberScrollState()

    val uiState by viewModel.uiState.collectAsState()
    val showLoading = rememberStabilizedLoading(uiState.loading)
    val waitingForCodeMessage = stringResource(R.string.otp_waiting_for_code_message)
    val color = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens



    LaunchedEffect(uiState.verified) {
        if (uiState.verified) {
            viewModel.finalizeVerifiedUser(
                onSuccess = onContinue,
                onError = {}
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Dimens.screenPadding)
                .padding(top = Dimens.mediumSpacing, bottom = Dimens.mediumSpacing)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(onClick = onBack, enabled = !uiState.loading) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = color.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

            Text(
                text = stringResource(R.string.otp_verify_phone_title),
                style = typography.heading4,
                color = color.textPrimary
            )

            Spacer(modifier = Modifier.height(Dimens.smallSpacing))

            Text(
                text = stringResource(R.string.otp_verify_phone_subtitle, phoneNumber),
                style = typography.bodyMedium,
                color = color.textPrimary
            )

            if (uiState.awaitingCode) {
                Spacer(modifier = Modifier.height(Dimens.smallSpacing))
                Text(
                    text = waitingForCodeMessage,
                    style = typography.bodySmall,
                    color = color.surfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(Dimens.largeSpacing))

            OtpTextFieldRow(
                otpDigits = otpDigits,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.verificationReady && !uiState.verified && !uiState.loading,
                onDigitChanged = { index, value ->
                    viewModel.clearActionError()
                    val digits = value.filter { it.isDigit() }
                    if (digits.isEmpty() && value.isNotEmpty()) {
                        return@OtpTextFieldRow
                    }

                    if (digits.length > 1) {
                        otpDigits = applyOtpPaste(
                            current = otpDigits,
                            fromIndex = index,
                            pastedDigits = digits
                        )
                        if (otpDigits.all { it.length == 1 }) {
                            keyboardController?.hide()
                        }
                        return@OtpTextFieldRow
                    }

                    val singleValue = digits.take(1)
                    if (singleValue.length <= 1) {
                        val updatedDigits = otpDigits.toMutableList()
                        updatedDigits[index] = singleValue
                        otpDigits = updatedDigits

                        if (singleValue.isNotEmpty() && index < 5) {
                            focusManager.moveFocus(FocusDirection.Next)
                        } else if (index == 5 && singleValue.isNotEmpty()) {
                            keyboardController?.hide()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

            if (uiState.verificationReady && !uiState.isResendAvailable) {
                Text(
                    text = stringResource(R.string.otp_resend_in_seconds, uiState.remainingSeconds),
                    style = typography.bodyMedium.copy(fontSize = 14.sp),
                    color = color.textPrimary
                )
            } else if (uiState.verificationReady) {
                TextButton(
                    onClick = {
                        if (activity != null) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.resendOtp(
                                phoneNumber = phoneNumber,
                                activity = activity,
                                onError = {}
                            )
                        }
                    },
                    enabled = !uiState.isResending && !uiState.loading
                ) {
                    Text(
                        if (uiState.isResending) {
                            stringResource(R.string.otp_resending_action)
                        } else {
                            stringResource(R.string.otp_resend_action)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.largeSpacing))

            PrimaryButton(
                onClick = {
                    if (uiState.verified) {
                        viewModel.finalizeVerifiedUser(
                            onSuccess = onContinue,
                            onError = {}
                        )
                    } else {
                        viewModel.verifyOtp(
                            code = otpDigits.joinToString(""),
                            onSuccess = { },
                            onError = { }
                        )
                    }
                },
                enabled = if (uiState.verified) {
                    !uiState.loading
                } else {
                    uiState.verificationReady && isOtpComplete && !uiState.loading
                },
                isLoading = uiState.loading,
                loadingText = stringResource(R.string.otp_verifying_action),
                text = if (uiState.verified) {
                    stringResource(R.string.otp_continue_action)
                } else {
                    stringResource(R.string.otp_continue_action)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight)
            )

            uiState.displayErrorMessage?.let { message ->
                Spacer(modifier = Modifier.height(Dimens.smallSpacing))
                Text(
                    text = message,
                    color = color.error,
                    style = typography.bodySmall
                )
            }
        }

        if (showLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color.textPrimary)
                    .pointerInput(Unit) {}
            )
            LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = stringResource(R.string.otp_loading_message)
            )
        }
    }
}

private fun applyOtpPaste(
    current: List<String>,
    fromIndex: Int,
    pastedDigits: String
): List<String> {
    val updated = current.toMutableList()
    var pointer = fromIndex.coerceIn(0, updated.lastIndex)
    pastedDigits.forEach { digit ->
        if (pointer > updated.lastIndex) return@forEach
        updated[pointer] = digit.toString()
        pointer++
    }
    return updated
}

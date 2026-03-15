package net.metalbrain.paysmart.core.features.account.creation.phone.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.OTPViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.OtpTextFieldRow
import net.metalbrain.paysmart.ui.screens.loader.LoadingState
import net.metalbrain.paysmart.ui.screens.loader.rememberStabilizedLoading
import net.metalbrain.paysmart.ui.theme.Dimens

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
    var submitting by remember { mutableStateOf(false) }
    var resending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val uiState by viewModel.uiState.collectAsState()
    val showLoading = rememberStabilizedLoading(uiState.loading)
    val otpFinishSetupErrorText = stringResource(R.string.otp_finish_setup_error)
    val otpInvalidCodeText = stringResource(R.string.otp_invalid_code_error)

    if (showLoading) {
        LoadingState(message = stringResource(R.string.otp_loading_message))
        return
    }

    var timeLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        modifier = modifier
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
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        Text(
            text = stringResource(R.string.otp_verify_phone_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(Dimens.smallSpacing))

        Text(
            text = stringResource(R.string.otp_verify_phone_subtitle, phoneNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.largeSpacing))

        OtpTextFieldRow(
            otpDigits = otpDigits,
            modifier = Modifier.fillMaxWidth(),
            onDigitChanged = { index, value ->
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

        if (timeLeft > 0) {
            Text(
                text = stringResource(R.string.otp_resend_in_seconds, timeLeft),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        } else {
            TextButton(
                onClick = {
                    resending = true
                    if (activity != null) {
                        viewModel.startTimer(backoff = false)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.resendOtp(
                            phoneNumber = phoneNumber,
                            activity = activity,
                            onSuccess = {
                                timeLeft = 60
                                resending = false
                            },
                            onError = {
                                resending = false
                            }
                        )
                    }
                },
                enabled = !resending
            ) {
                Text(
                    if (resending) {
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
                val code = otpDigits.joinToString("")
                submitting = true
                errorMessage = null

                viewModel.verifyOtp(
                    code = code,
                    onSuccess = {
                        viewModel.upsertUserAfterOtp(
                            onDone = {
                                submitting = false
                                onContinue()
                            },
                            onError = { error ->
                                submitting = false
                                errorMessage = error.message
                                    ?: otpFinishSetupErrorText
                            }
                        )
                    },
                    onError = { e ->
                        submitting = false
                        errorMessage = e.message ?: otpInvalidCodeText
                    }
                )
            },
            enabled = isOtpComplete,
            isLoading = submitting,
            loadingText = stringResource(R.string.otp_verifying_action),
            text = stringResource(R.string.otp_continue_action),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(Dimens.smallSpacing))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
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

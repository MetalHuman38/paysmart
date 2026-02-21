package net.metalbrain.paysmart.phone

import net.metalbrain.paysmart.ui.components.OtpTextFieldRow
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.screens.LoadingState
import net.metalbrain.paysmart.ui.screens.rememberStabilizedLoading

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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val showLoading = rememberStabilizedLoading(uiState.loading)

    // 🌀 Show animated spinner while loading
    if (showLoading) {
        LoadingState(message = "Creating your account....")
        return
    }

    var timeLeft by remember { mutableIntStateOf(60) }

    // ⏱ Countdown timer
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 🔙 Back
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verify phone number",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6 digit code sent to $phoneNumber",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OtpTextFieldRow(
            otpDigits = otpDigits,
            onDigitChanged = { index, value ->
                if (value.length <= 1 && value.all { it.isDigit() }) {
                    val updatedDigits = otpDigits.toMutableList()
                    updatedDigits[index] = value
                    otpDigits = updatedDigits

                    // Auto move
                    if (value.isNotEmpty() && index < 5) {
                        focusManager.moveFocus(FocusDirection.Next)
                    } else if (index == 5 && value.isNotEmpty()) {
                        keyboardController?.hide()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (timeLeft > 0) {
            Text(
                text = "Didn't receive it? Retry in $timeLeft seconds",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        } else {
            var resending by remember { mutableStateOf(false) }

            TextButton(
                onClick = {
                    resending = true
                    if(activity != null) {
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
                Text(if (resending) "Resending..." else "Resend OTP")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            onClick = {
                val code = otpDigits.joinToString("")
                submitting = true
                errorMessage = null

                viewModel.verifyOtp(
                    code = code,
                    onSuccess = {
                        submitting = false
                        viewModel.upsertUserAfterOtp()
                        onContinue()
                    },
                    onError = { e ->
                        submitting = false
                        errorMessage = e.message ?: "Invalid verification code"
                    }
                )
            },
            enabled = isOtpComplete,
            isLoading = submitting,
            loadingText = "Verifying...",
            text = "Continue",
                    modifier = Modifier
                         .fillMaxWidth()
                         .height(52.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

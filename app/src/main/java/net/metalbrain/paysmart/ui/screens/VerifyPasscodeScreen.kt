package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.ui.components.NumberPad
import net.metalbrain.paysmart.ui.viewmodel.VerifyPasscodeViewModel
import net.metalbrain.paysmart.utils.launchBiometricPrompt
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isLockedOut by viewModel.isLockedOut.collectAsState()
    val shakeTrigger by viewModel.shakeTrigger


    if (isLockedOut) {
        Text(
            text = "Too many attempts. Please wait...",
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
    }

    LaunchedEffect(biometricPrompt) {
        if (biometricPrompt) {
            launchBiometricPrompt(
                context = context,
                onSuccess = {
                    viewModel.onBiometricDismissed()
                    onVerified()
                },
                onFail = {
                    viewModel.onBiometricDismissed()
                    scope.launch {
                        snackbarHostState.showSnackbar("Biometric authentication failed")
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SnackbarHost(hostState = snackbarHostState)

        Text("Enter your passcode", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        // Dots display
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.shake(trigger = shakeTrigger)
        ) {

            repeat(6) { index ->
                val filled = index < passcode.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (filled) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (error != null) {
            // 🔔 Haptic feedback
            LaunchedEffect(error) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            Text(error ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        NumberPad(
            onDigitPressed = { if (!isLockedOut) viewModel.appendDigit(it) },
            onBackspace = { if (!isLockedOut) viewModel.removeLastDigit() }
        )
    }
}

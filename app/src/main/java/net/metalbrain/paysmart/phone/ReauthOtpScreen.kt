package net.metalbrain.paysmart.phone

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.screens.AppLoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReauthOtpScreen(
    viewModel: ReauthOtpViewModel,
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val code by viewModel.code
    LaunchedEffect(Unit) {
        Log.d("ReauthOtpScreen", "LaunchedEffect triggered")
        viewModel.startReauthFlow(activity)
    }

    // 🌀 Show animated spinner while loading
    if (uiState.isLoading) {
        AppLoadingScreen(message = "Signing in...")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reauthenticate") },
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
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("For your security, please verify your identity")

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { viewModel.onCodeChange(it) },
                label = { Text("Enter OTP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Verify",
                onClick = { viewModel.reauthWithCode(onSuccess) },
                enabled = code.length == 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.resendAvailable) {
                TextButton(onClick = { viewModel.resendOtp(activity) }) {
                    Text("Resend Code")
                }
            } else {
                Text("Resend available in ${uiState.timerSeconds}s")
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

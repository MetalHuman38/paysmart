package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.ui.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.BiometricToggleRow
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun BiometricOptInScreen(
    viewModel: BiometricOptInViewModel,
    idToken: String,
    activity: FragmentActivity,
    onSkip: () -> Unit,
    onSuccess: () -> Unit
) {
    val biometricAvailable by viewModel.biometricAvailable.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val lockedOut by viewModel.lockedOut.collectAsState()

    // Lottie Setup 🎞️
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.biometric))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    // Check availability on first load
    LaunchedEffect(Unit) {
        viewModel.checkBiometricSupport(activity)
    }

    // Handle success nav
    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔐 Lottie Animation
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(160.dp),
            enableMergePaths = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Protect your account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Adding biometric security will ensure you are the only one that can access your PaySmart account.\nIt also protects your secure activities.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🌐 Biometric Toggle
        BiometricToggleRow(
            enabled = biometricEnabled,
            onToggle = {
                if (!isLoading && biometricAvailable && !lockedOut) {
                    viewModel.enableBiometric(activity, idToken)
                }
            },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Skip, I’ll do this later",
            modifier = Modifier
                .clickable { onSkip() }
                .padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

package net.metalbrain.paysmart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.ui.screens.AppLoadingScreen
import net.metalbrain.paysmart.ui.screens.BiometricOptInScreen
import net.metalbrain.paysmart.ui.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel


@Composable
fun BiometricOptInSetupGuard(
    viewModel: SecurityViewModel,
    bioMetricViewModel: BiometricOptInViewModel,
    idToken: String,
    activity: FragmentActivity,
    onBiometricOptIn: () -> Unit,
    content: @Composable () -> Unit
) {
    val securitySettings by viewModel.securitySettings.collectAsState()

    if (securitySettings == null) {
        AppLoadingScreen(message = "Loading security settings...")
        return
    }

    if (securitySettings?.biometricsRequired == true) {
        BiometricOptInScreen(
            viewModel = bioMetricViewModel,
            idToken = idToken,
            activity = activity,
            onSkip = onBiometricOptIn,
            onSuccess = onBiometricOptIn
        )
    } else {
        content()
    }
}

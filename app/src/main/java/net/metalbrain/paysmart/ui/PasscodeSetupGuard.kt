package net.metalbrain.paysmart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.ui.screens.AppLoadingScreen
import net.metalbrain.paysmart.ui.screens.SetPasscodeScreen
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel


@Composable
fun PasscodeSetupGuard(
    viewModel: SecurityViewModel,
    onPasscodeSet: () -> Unit,
    content: @Composable () -> Unit
) {
    val securitySettings by viewModel.securitySettings.collectAsState()

    if (securitySettings == null) {
        AppLoadingScreen(message = "Loading security settings...")
        return
    }

    if (securitySettings?.passcodeEnabled == true) {
        content()
    } else {
        SetPasscodeScreen(
            onPasscodeSet = onPasscodeSet
        )
    }
}

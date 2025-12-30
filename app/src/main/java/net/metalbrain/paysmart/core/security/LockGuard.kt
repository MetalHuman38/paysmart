package net.metalbrain.paysmart.core.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun LockGuard(
    viewModel: SecurityViewModel,
    idleMinutes: Int = 5,
    content: @Composable () -> Unit
) {
    val isLocked by viewModel.isLocked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkIfLocked()
    }

    if (isLocked) {
        PasscodePrompt(
            onVerified = { viewModel.unlockSession() }
        )
    } else {
        content()
    }
}

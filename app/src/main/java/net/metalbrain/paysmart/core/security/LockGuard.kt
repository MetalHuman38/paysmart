package net.metalbrain.paysmart.core.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun LockGuard(
    viewModel: SecurityViewModel,
    user: AuthUserModel,
    idleMinutes: Int = 5,
    content: @Composable () -> Unit
) {
    val isLocked by viewModel.isLocked.collectAsState()

    // Coroutine scope tied to this composable
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.checkIfLocked()
    }

    if (isLocked) {
        PasscodePrompt(
            onVerified = {
                scope.launch {
                    viewModel.unlockSession()
                }
            },
            user = user,
        )
    } else {
        content()
    }
}

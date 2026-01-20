package net.metalbrain.paysmart

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun IdleLockOverlay(
    viewModel: SecurityViewModel,
    content: @Composable () -> Unit
) {
    val settings by viewModel.securitySettings.collectAsState()
    var lastActivity by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val activityModifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            lastActivity = System.currentTimeMillis()
        }
    }

    LaunchedEffect(settings) {
        if (settings?.passcodeEnabled != true) return@LaunchedEffect

        while (true) {
            delay(1_000)
            val timeout = (settings!!.lockAfterMinutes ?: 5) * 60_000
            val due = System.currentTimeMillis() - lastActivity > timeout

            if (due && viewModel.hasPasscode()) {
                viewModel.checkIfLocked()
            }
        }
    }

    Box(modifier = activityModifier.fillMaxSize()) {
        content()
    }
}

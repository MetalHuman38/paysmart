package net.metalbrain.paysmart

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.DurationUnit

@OptIn(ExperimentalTime::class)
@Composable
fun IdleLockOverlay(
    viewModel: UserViewModel,
    content: @Composable () -> Unit
) {
    var lastActivity by remember { mutableStateOf(Clock.System.now()) }
    var prompting by remember { mutableStateOf(false) }

    LocalContext.current
    rememberCoroutineScope()

    // Watch user input to reset idle
    val activityModifier = Modifier.pointerInput(Unit) {
        detectTapGestures { lastActivity = Clock.System.now() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            val settings = viewModel.getSecuritySettings()
            if (settings?.passcodeEnabled == true) {
                val timeout = (settings.lockAfterMinutes ?: 5).toDuration(DurationUnit.MINUTES)
                val due = lastActivity + timeout < Clock.System.now()

                if (due && !prompting) {
                    val hasPasscode = viewModel.hasLocalPasscode()
                    if (hasPasscode) {
                        prompting = true
                        val ok = viewModel.showPasscodePrompt()
                        if (ok) {
                            lastActivity = Clock.System.now()
                        }
                        prompting = false
                    }
                }
            }
        }
    }

    Box(modifier = activityModifier.fillMaxSize()) {
        content()
    }
}

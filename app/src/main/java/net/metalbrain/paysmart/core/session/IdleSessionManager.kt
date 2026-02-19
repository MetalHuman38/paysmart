package net.metalbrain.paysmart.core.session

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

@Composable
fun IdleSessionWatcher(
    enabled: Boolean,
    lockAfterMinutes: Int,
    onTimeout: () -> Unit,
    onInteraction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val activityModifier = if (enabled) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent()
                    lastInteraction = System.currentTimeMillis()
                    onInteraction?.invoke()
                }
            }
        }
    } else {
        Modifier
    }

    LaunchedEffect(enabled, lockAfterMinutes) {
        if (!enabled) {
            return@LaunchedEffect
        }

        val timeoutMs = lockAfterMinutes * 60_000L
        while (true) {
            delay(1_000)
            val idleFor = System.currentTimeMillis() - lastInteraction
            if (idleFor >= timeoutMs) {
                Log.d(
                    "IdleSessionWatcher",
                    "Idle timeout reached: idleForMs=$idleFor, timeoutMs=$timeoutMs, lockAfterMinutes=$lockAfterMinutes"
                )
                onTimeout()
                lastInteraction = System.currentTimeMillis()
            }
        }
    }

    Box(
        modifier = activityModifier.fillMaxSize()
    ) {
        content()
    }
}

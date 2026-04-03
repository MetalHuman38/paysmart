package net.metalbrain.paysmart.ui.screens.loader

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun stabilizedLoading(
    phase: LoadingPhase,
    showDelayMillis: Long = 150L,
    minVisibleMillis: Long = 400L
): Boolean {
    val isLoading = phase != LoadingPhase.Idle
    var isVisible by remember { mutableStateOf(false) }
    var visibleSince by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            if (showDelayMillis > 0) {
                delay(showDelayMillis)
            }
            isVisible = true
            visibleSince = SystemClock.elapsedRealtime()
            return@LaunchedEffect
        }

        if (!isVisible) {
            return@LaunchedEffect
        }

        val elapsed = SystemClock.elapsedRealtime() - visibleSince
        val remaining = (minVisibleMillis - elapsed).coerceAtLeast(0L)
        if (remaining > 0) {
            delay(remaining)
        }
        isVisible = false
    }

    return isVisible
}

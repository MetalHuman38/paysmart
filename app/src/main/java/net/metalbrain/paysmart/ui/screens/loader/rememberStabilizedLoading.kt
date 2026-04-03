package net.metalbrain.paysmart.ui.screens.loader

import androidx.compose.runtime.Composable

@Composable
fun rememberStabilizedLoading(
    phase: LoadingPhase,
    showDelayMillis: Long = 150L,
    minVisibleMillis: Long = 400L
): Boolean {
    return stabilizedLoading(
        phase = phase,
        showDelayMillis = showDelayMillis,
        minVisibleMillis = minVisibleMillis
    )
}

@Composable
fun rememberStabilizedLoading(
    isLoading: Boolean,
    showDelayMillis: Long = 150L,
    minVisibleMillis: Long = 400L
): Boolean {
    return stabilizedLoading(
        phase = if (isLoading) LoadingPhase.Processing else LoadingPhase.Idle,
        showDelayMillis = showDelayMillis,
        minVisibleMillis = minVisibleMillis
    )
}

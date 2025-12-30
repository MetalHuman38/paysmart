package net.metalbrain.paysmart.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay


@Composable
fun rememberDebouncedState(
    input: String,
    delayMillis: Long = 300L
): State<String> {
    val debouncedState = remember { mutableStateOf(input) }

    LaunchedEffect(input) {
        delay(delayMillis)
        debouncedState.value = input
    }

    return debouncedState
}

package net.metalbrain.paysmart.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.shake(trigger: Long): Modifier {
    val density = LocalDensity.current
    val shakeDeltaPx = remember(density) { with(density) { 16.dp.toPx() } }

    val offsetX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = keyframes {
            durationMillis = 500
            shakeDeltaPx at 100
            shakeDeltaPx at 200
            shakeDeltaPx at 300
            shakeDeltaPx at 400
        },
        label = "Shake"
    )

    return this.offset { IntOffset(offsetX.toInt(), 0) }
}

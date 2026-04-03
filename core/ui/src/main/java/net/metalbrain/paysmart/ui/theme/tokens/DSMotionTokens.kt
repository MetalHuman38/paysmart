package net.metalbrain.paysmart.ui.theme.tokens

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

object DSMotionTokens {
    const val DURATION_FAST: Int = 120
    const val DURATION_MEDIUM: Int = 200
    const val DURATION_SLOW: Int = 300

    // Standard ease-in-out: for elements moving within the screen
    val emphasisStandard: Easing = FastOutSlowInEasing

    // Decelerate (ease-out): for elements entering the screen
    val emphasisDecelerate: Easing = LinearOutSlowInEasing

    // Accelerate (ease-in): for elements leaving the screen
    val emphasisAccelerate: Easing = FastOutLinearInEasing
}

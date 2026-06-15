package net.metalbrain.paysmart.ui.screens.loader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.R

/**
 * A composable screen that displays the application's loading state.
 *
 * It wraps the [LoadingState] component with a specific brand animation (coin spin)
 * to provide a consistent loading experience across the app.
 *
 * @param modifier The [Modifier] to be applied to the loading screen layout.
 * @param phase The current [LoadingPhase] indicating the stage of the loading process.
 * @param message An optional primary message to display to the user.
 * @param hint An optional secondary message or tip to show while waiting.
 */
@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier,
    phase: LoadingPhase = LoadingPhase.Startup,
    message: String? = null,
    hint: String? = null,
) {
    LoadingState(
        modifier = modifier,
        phase = phase,
        message = message,
        hint = hint,
        animationRes = R.raw.coin_spin
    )
}

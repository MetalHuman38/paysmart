package net.metalbrain.paysmart.ui.screens

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.screens.loader.LoadingPhase
import net.metalbrain.paysmart.ui.screens.loader.LoadingState

/**
 * Composable that displays the splash screen of the application.
 *
 * It utilizes the [LoadingState] component to represent the current progress or status
 * during the initial application boot or data loading sequence.
 *
 * @param phase The current [LoadingPhase] to be displayed, defaulting to [LoadingPhase.Startup].
 */
@Composable
fun SplashScreen(
    phase: LoadingPhase = LoadingPhase.Startup
) {
    LoadingState(phase = phase)
}

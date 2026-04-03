package net.metalbrain.paysmart.ui.screens

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.screens.loader.LoadingPhase
import net.metalbrain.paysmart.ui.screens.loader.LoadingState

@Composable
fun SplashScreen(
    phase: LoadingPhase = LoadingPhase.Startup
) {
    LoadingState(phase = phase)
}

package net.metalbrain.paysmart.ui.screens.loader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.R

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

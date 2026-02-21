package net.metalbrain.paysmart.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.R

@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
) {
    LoadingState(
        modifier = modifier,
        message = message,
        animationRes = R.raw.coin_spin
    )
}

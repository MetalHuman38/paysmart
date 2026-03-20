package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.animate.AnimatedLottieBackground
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun StartupAnimationStage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(vertical = Dimens.sm),
        contentAlignment = Alignment.Center
    ) {
        AnimatedLottieBackground(
            modifier = Modifier.fillMaxSize()
        )
    }
}

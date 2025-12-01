package net.metalbrain.paysmart.ui.animate

import androidx.compose.animation.core.*
import net.metalbrain.paysmart.R
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@Composable
fun AnimatedLottieBackground(modifier: Modifier = Modifier) {
    val animations = listOf(
        R.raw.bg_1,
        R.raw.grow,
        R.raw.wallet
    )

    var currentIndex by remember { mutableIntStateOf(0) }
    val alphaAnim = remember { Animatable(1f) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animations[currentIndex])
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(currentIndex) {
        while (true) {
            alphaAnim.animateTo(1f, animationSpec = tween(1000)) // Fade in
            delay(4000) // Show animation
            alphaAnim.animateTo(0f, animationSpec = tween(1000)) // Fade out
            currentIndex = (currentIndex + 1) % animations.size
        }
    }

    Box(modifier = modifier) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        modifier = Modifier.graphicsLayer {
                alpha = alphaAnim.value
            }
        )
    }
}

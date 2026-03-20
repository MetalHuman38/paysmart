package net.metalbrain.paysmart.ui.animate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.R

@Composable
fun AnimatedLottieBackground(modifier: Modifier = Modifier) {
    val animations = listOf(
        R.raw.card,
        R.raw.grow,
        R.raw.wallet
    )

    var currentIndex by remember { mutableIntStateOf(0) }
    val alphaAnim = remember { Animatable(1f) }
    val darkMode = MaterialTheme.colorScheme.background.luminance() < 0.2f

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animations[currentIndex])
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(currentIndex) {
        while (true) {
            alphaAnim.animateTo(1f, animationSpec = tween(1000))
            delay(4000)
            alphaAnim.animateTo(0f, animationSpec = tween(1000))
            currentIndex = (currentIndex + 1) % animations.size
        }
    }

    Box(modifier = modifier) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = alphaAnim.value * if (darkMode) 0.82f else 1f
                }
        )
    }
}

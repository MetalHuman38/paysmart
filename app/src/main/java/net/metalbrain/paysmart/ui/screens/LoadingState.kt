package net.metalbrain.paysmart.ui.screens


import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.R

@Composable
fun rememberStabilizedLoading(
    isLoading: Boolean,
    showDelayMillis: Long = 150L,
    minVisibleMillis: Long = 400L
): Boolean {
    var isVisible by remember { mutableStateOf(false) }
    var visibleSince by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            if (showDelayMillis > 0) {
                delay(showDelayMillis)
            }
            isVisible = true
            visibleSince = SystemClock.elapsedRealtime()
            return@LaunchedEffect
        }

        if (!isVisible) {
            return@LaunchedEffect
        }

        val elapsed = SystemClock.elapsedRealtime() - visibleSince
        val remaining = (minVisibleMillis - elapsed).coerceAtLeast(0L)
        if (remaining > 0) {
            delay(remaining)
        }
        isVisible = false
    }

    return isVisible
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    animationRes: Int = R.raw.loader
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

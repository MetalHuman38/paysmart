package net.metalbrain.paysmart.ui.screens.loader


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    phase: LoadingPhase = LoadingPhase.Processing,
    message: String? = null,
    hint: String? = null,
    animationRes: Int = R.raw.loader
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )
    val showLoading = stabilizedLoading(phase)
    var displayPhase by remember { mutableStateOf(phase) }

    LaunchedEffect(phase, showLoading) {
        if (phase != LoadingPhase.Idle) {
            displayPhase = phase
        } else if (!showLoading) {
            displayPhase = LoadingPhase.Idle
        }
    }

    val messageSpec = remember(displayPhase, message, hint) {
        when {
            !message.isNullOrBlank() -> LoadingMessageSpec(message = message, hint = hint)
            displayPhase == LoadingPhase.Idle -> LoadingMessageSpec(message = "Loading...")
            else -> resolveMessage(displayPhase)
        }
    }

    Crossfade(
        targetState = showLoading,
        label = "loading_visibility"
    ) { isLoading ->
        if (!isLoading) {
            return@Crossfade
        }

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
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = messageSpec,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "loading_message"
            ) { content ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = content.message,
                        style = PaysmartTheme.typographyTokens.heading4,
                        color = PaysmartTheme.colorTokens.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    content.hint?.takeIf { it.isNotBlank() }?.let { helperText ->
                        Text(
                            text = helperText,
                            style = PaysmartTheme.typographyTokens.bodyMedium,
                            color = PaysmartTheme.colorTokens.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

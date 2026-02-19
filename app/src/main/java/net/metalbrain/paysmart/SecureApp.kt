package net.metalbrain.paysmart


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.auth.state.SecureNavIntent
import net.metalbrain.paysmart.ui.screens.SplashScreen

@Composable
fun SecureApp(
    postAuthState: PostAuthState,
    minimumSplashMs: Long = 900L,
    onIntent: (SecureNavIntent) -> Unit,
) {
    var minSplashPassed by remember { mutableStateOf(false) }

    // ✅ UX polish: minimum splash duration (even if states resolve instantly)
    LaunchedEffect(Unit) {
        delay(minimumSplashMs)
        minSplashPassed = true
    }

    // ✅ Emit intents deterministically (no NavController here)
    LaunchedEffect(postAuthState, minSplashPassed) {
        if (!minSplashPassed) return@LaunchedEffect
        val intent = when (postAuthState) {
            PostAuthState.Loading -> SecureNavIntent.None
            PostAuthState.Unauthenticated -> SecureNavIntent.ToStartup
            PostAuthState.RequireAccountProtection -> SecureNavIntent.ToAccountProtection
            PostAuthState.RequireEmailVerification -> SecureNavIntent.ToEmailVerification
            PostAuthState.Locked -> SecureNavIntent.RequireSessionUnlock
            PostAuthState.Ready -> SecureNavIntent.None
        }

        if (intent != SecureNavIntent.None) onIntent(intent)
    }

    // ✅ Render overlays (pure rendering)
    when {
        !minSplashPassed || postAuthState is PostAuthState.Loading -> {
            SplashScreen()
        }

        else -> Unit
    }
}

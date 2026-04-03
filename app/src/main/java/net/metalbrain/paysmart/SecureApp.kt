package net.metalbrain.paysmart


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.auth.state.SecureNavIntent

@Composable
fun SecureApp(
    postAuthState: PostAuthState,
    onIntent: (SecureNavIntent) -> Unit,
) {
    LaunchedEffect(postAuthState) {
        val intent = when (postAuthState) {
            PostAuthState.Loading -> SecureNavIntent.None
            PostAuthState.Unauthenticated -> SecureNavIntent.ToStartup
            PostAuthState.RequireAccountProtection -> SecureNavIntent.ToAccountProtection
            PostAuthState.RequirePasswordSetup -> SecureNavIntent.ToCreatePassword
            PostAuthState.RequirePasswordRecovery -> SecureNavIntent.ToPasswordRecovery
            PostAuthState.RequireEmailVerification -> SecureNavIntent.ToEmailVerification
            PostAuthState.Locked -> SecureNavIntent.RequireSessionUnlock
            PostAuthState.Ready -> SecureNavIntent.None
        }

        if (intent != SecureNavIntent.None) onIntent(intent)
    }
}

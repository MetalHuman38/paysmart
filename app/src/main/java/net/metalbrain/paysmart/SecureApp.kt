package net.metalbrain.paysmart


import android.util.Log
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
            PostAuthState.RequireRecoveryMethod -> SecureNavIntent.ToRecoveryMethod
            PostAuthState.RequireRecoveryPassword -> SecureNavIntent.ToCreatePassword
            PostAuthState.RequirePasswordRecovery -> SecureNavIntent.ToPasswordRecovery
            PostAuthState.Locked -> SecureNavIntent.RequireSessionUnlock
            PostAuthState.Ready -> SecureNavIntent.None
        }

        Log.d("SecureNavTrace", "postAuthState=$postAuthState intent=$intent")

        if (intent != SecureNavIntent.None) onIntent(intent)
    }
}

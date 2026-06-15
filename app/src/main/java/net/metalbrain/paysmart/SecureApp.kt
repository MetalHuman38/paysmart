package net.metalbrain.paysmart


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.auth.state.SecureNavIntent

/**
 * Monitors the [postAuthState] and dispatches navigation intents via [onIntent] based on the current
 * authentication and initialization status of the application.
 *
 * This component acts as a security gatekeeper, ensuring the user is directed to the appropriate
 * screen (e.g., login, recovery, or session unlock) whenever the authentication state changes.
 *
 * @param postAuthState The current state of the application post-authentication check.
 * @param onIntent A callback triggered when a specific navigation action is required to progress
 * through the security or initialization flow.
 */
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
            PostAuthState.RequireRecoveryPassword -> SecureNavIntent.ToRecoveryPassword
            PostAuthState.RequirePasswordRecovery -> SecureNavIntent.ToPasswordRecovery
            PostAuthState.Locked -> SecureNavIntent.RequireSessionUnlock
            PostAuthState.Ready -> SecureNavIntent.None
        }

        Log.d("SecureNavTrace", "postAuthState=$postAuthState intent=$intent")

        if (intent != SecureNavIntent.None) onIntent(intent)
    }
}

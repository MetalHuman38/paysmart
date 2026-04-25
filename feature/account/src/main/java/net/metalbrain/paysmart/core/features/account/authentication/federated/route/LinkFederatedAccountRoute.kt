package net.metalbrain.paysmart.core.features.account.authentication.federated.route

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.authentication.federated.screen.FederatedLinkingScreen
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.navigator.Screen

@Composable
fun LinkFederatedAccountRoute(
    clientId: String,
    returnRoute: String,
    hasReadyPassword: Boolean,
    hasRecoveryMethod: Boolean,
    onRecoveryMethodReady: () -> Unit,
    onNavigateToDestination: (destination: String) -> Unit,
    onNavigateToPasskeySetup: () -> Unit,
    onNavigateToAddEmail: () -> Unit,
) {
    val destination = returnRoute.ifBlank {
        if (hasReadyPassword) Screen.Home.route else Screen.CreatePassword.BASEROUTE
    }
    val linkViewModel: LoginViewModel = hiltViewModel()

    FederatedLinkingScreen(
        isLoading = linkViewModel.loading,
        clientId = clientId,
        onLinkGoogleCredential = { credential, onError ->
            linkViewModel.linkFederatedCredential(
                credential = credential,
                onSuccess = {
                    onRecoveryMethodReady()
                    onNavigateToDestination(destination)
                },
                onError = onError,
            )
        },
        onFacebookLogin = { activity, onError ->
            linkViewModel.handleFacebookLogin(
                activity = activity,
                onSuccess = {
                    onRecoveryMethodReady()
                    onNavigateToDestination(destination)
                },
                onMfaRequired = {},
                onError = onError,
            )
        },
        onPasskeySetupClick = onNavigateToPasskeySetup,
        onEmailVerifyClick = onNavigateToAddEmail,
        onSkip = {
            if (hasRecoveryMethod) {
                onNavigateToDestination(destination)
            }
        },
    )
}

package net.metalbrain.paysmart.core.features.account.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.metalbrain.paysmart.core.features.account.authentication.email.route.AddEmailRoute
import net.metalbrain.paysmart.core.features.account.authentication.email.route.EmailSentRoute
import net.metalbrain.paysmart.core.features.account.authentication.email.route.EmailVerifiedRoute
import net.metalbrain.paysmart.core.features.account.authentication.login.route.LoginRoute
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaSignInChallengeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaSignInViewModel
import net.metalbrain.paysmart.navigator.Screen

fun NavGraphBuilder.accountAuthRoutes(
    clientId: @Composable () -> String,
    currentLanguage: @Composable () -> String,
    // Login
    onLoginSuccess: () -> Unit,
    onLoginMfaRequired: () -> Unit,
    onLoginForgotPassword: () -> Unit,
    onLoginSignUp: () -> Unit,
    onLoginBack: () -> Unit,
    onLoginNavigateToLanguage: () -> Unit,
    onLoginNavigateToReauth: () -> Unit,
    onLoginEmailSent: (email: String) -> Unit,
    // LoginMfaChallenge
    onMfaChallengeBack: () -> Unit,
    onMfaChallengeSuccess: () -> Unit,
    // AddEmail
    onAddEmailSent: (email: String, returnRoute: String) -> Unit,
    // EmailSent + EmailVerified shared — composable slot that produces the mark-recovery action
    // so SecurityViewModel can be looked up in composable scope inside :app and passed as a
    // regular lambda to EmailSentRoute / EmailVerifiedRoute without leaking the ViewModel type.
    onMarkRecoveryReady: @Composable () -> () -> Unit,
    // EmailSent
    onEmailSentNavigateToVerified: (returnRoute: String) -> Unit,
    onEmailSentBack: () -> Unit,
    // EmailVerified
    onEmailVerifiedBackToApp: (returnRoute: String) -> Unit,
) {
    composable(Screen.Login.route) {
        LoginRoute(
            clientId = clientId(),
            currentLanguage = currentLanguage(),
            onContinue = onLoginSuccess,
            onMfaRequired = onLoginMfaRequired,
            onForgotPassword = onLoginForgotPassword,
            onSignUp = onLoginSignUp,
            onBackClicked = onLoginBack,
            onNavigateToLanguage = onLoginNavigateToLanguage,
            onNavigateToReauth = onLoginNavigateToReauth,
            onNavigateToEmailSent = onLoginEmailSent,
        )
    }

    composable(Screen.LoginMfaChallenge.route) {
        val viewModel: MfaSignInViewModel = hiltViewModel()
        MfaSignInChallengeScreen(
            viewModel = viewModel,
            onBack = onMfaChallengeBack,
            onSuccess = onMfaChallengeSuccess,
        )
    }

    composable(
        route = "${Screen.AddEmail.route}?returnRoute={returnRoute}",
        arguments = listOf(
            navArgument("returnRoute") {
                type = NavType.StringType
                defaultValue = Screen.Home.route
            }
        )
    ) { backStackEntry ->
        val returnRoute = Uri.decode(
            backStackEntry.arguments?.getString("returnRoute") ?: Screen.Home.route
        )
        AddEmailRoute(
            returnRoute = returnRoute,
            onNavigateToEmailSent = { email -> onAddEmailSent(email, returnRoute) },
        )
    }

    composable(
        route = "${Screen.EmailSent.route}?returnRoute={returnRoute}",
        arguments = listOf(
            navArgument("email") { type = NavType.StringType },
            navArgument("returnRoute") {
                type = NavType.StringType
                defaultValue = Screen.Home.route
            }
        )
    ) { backStackEntry ->
        val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
        val returnRoute = Uri.decode(
            backStackEntry.arguments?.getString("returnRoute") ?: Screen.Home.route
        )
        val markRecoveryReady = onMarkRecoveryReady()
        EmailSentRoute(
            email = email,
            returnRoute = returnRoute,
            onVerified = {
                markRecoveryReady()
                onEmailSentNavigateToVerified(returnRoute)
            },
            onChangeEmail = onEmailSentBack,
            onBack = onEmailSentBack,
        )
    }

    composable(
        route = "${Screen.EmailVerified.route}?returnRoute={returnRoute}",
        arguments = listOf(
            navArgument("returnRoute") {
                type = NavType.StringType
                defaultValue = Screen.Home.route
            }
        )
    ) { backStackEntry ->
        val returnRoute = Uri.decode(
            backStackEntry.arguments?.getString("returnRoute") ?: Screen.Home.route
        )
        val markRecoveryReady = onMarkRecoveryReady()
        EmailVerifiedRoute(
            onRecoveryMethodReady = markRecoveryReady,
            onBackToApp = { onEmailVerifiedBackToApp(returnRoute) },
        )
    }
}

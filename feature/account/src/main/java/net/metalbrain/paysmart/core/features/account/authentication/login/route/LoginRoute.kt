package net.metalbrain.paysmart.core.features.account.authentication.login.route

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.authentication.login.screen.LoginScreen
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel

@Composable
fun LoginRoute(
    clientId: String,
    currentLanguage: String,
    onContinue: () -> Unit,
    onMfaRequired: () -> Unit,
    onBackClicked: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToReauth: () -> Unit,
    onNavigateToEmailSent: (email: String) -> Unit,
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val reauthOtpViewModel: ReauthOtpViewModel = hiltViewModel()

    LoginScreen(
        clientId = clientId,
        currentLanguage = currentLanguage,
        viewModel = loginViewModel,
        reauthOtpViewModel = reauthOtpViewModel,
        onContinue = onContinue,
        onMfaRequired = onMfaRequired,
        onBackClicked = onBackClicked,
        onForgotPassword = onForgotPassword,
        onSignUp = onSignUp,
        onNavigateToLanguage = onNavigateToLanguage,
        onNavigateToReauth = onNavigateToReauth,
        onNavigateToEmailSent = onNavigateToEmailSent,
    )
}

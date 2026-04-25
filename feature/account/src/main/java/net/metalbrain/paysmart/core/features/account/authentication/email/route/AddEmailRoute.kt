package net.metalbrain.paysmart.core.features.account.authentication.email.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.AddEmailScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.AddEmailViewModel

@Composable
fun AddEmailRoute(
    returnRoute: String,
    onNavigateToEmailSent: (email: String) -> Unit,
) {
    val viewModel: AddEmailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    AddEmailScreen(
        email = uiState.email,
        emailValid = uiState.emailValid,
        error = uiState.error,
        isLoading = uiState.loading,
        onEmailChanged = viewModel::onEmailChanged,
        onSendVerification = {
            viewModel.sendVerificationEmail(returnRoute = returnRoute) {
                onNavigateToEmailSent(uiState.email)
                viewModel.onEmailChanged("")
            }
        }
    )
}

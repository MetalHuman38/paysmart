package net.metalbrain.paysmart.core.features.account.authentication.email.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.EmailVerificationSuccessScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.EmailSentViewModel

@Composable
fun EmailVerifiedRoute(
    onBackToApp: () -> Unit,
    onRecoveryMethodReady: () -> Unit,
) {
    val viewModel: EmailSentViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        onRecoveryMethodReady()
        viewModel.refreshVerificationStatus(email = "", onVerified = {})
    }

    EmailVerificationSuccessScreen(onBackToApp = onBackToApp)
}

package net.metalbrain.paysmart.core.features.account.authentication.email.route

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.EmailSentScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.EmailSentViewModel

@Composable
fun EmailSentRoute(
    email: String,
    returnRoute: String,
    onVerified: () -> Unit,
    onChangeEmail: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: EmailSentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        val message = uiState.infoMessage ?: uiState.errorMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeTransientMessage()
        }
    }

    LaunchedEffect(email) {
        viewModel.refreshVerificationStatus(email = email, onVerified = onVerified)
    }

    DisposableEffect(lifecycleOwner, email) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshVerificationStatus(email = email, onVerified = onVerified)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    EmailSentScreen(
        email = email,
        onResend = { viewModel.resendVerificationEmail(email = email, returnRoute = returnRoute) },
        onOpenEmailApp = {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.packageManager.resolveActivity(intent, 0) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
            }
        },
        onChangeEmail = onChangeEmail,
        onBack = onBack,
        isResending = uiState.isResending,
        infoMessage = uiState.infoMessage,
        errorMessage = uiState.errorMessage
    )
}

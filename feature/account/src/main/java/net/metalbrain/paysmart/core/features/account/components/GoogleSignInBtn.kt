package net.metalbrain.paysmart.core.features.account.components

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthIntent
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthProvider
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.AuthProviderButton

@Composable
fun GoogleSignInBtn(
    clientId: String,
    modifier: Modifier = Modifier,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null,
    intent: GoogleAuthIntent = GoogleAuthIntent.SIGN_IN,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait...",
    onCredentialReceived: (AuthCredential) -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AuthProviderButton(
        text = stringResource(R.string.continue_with_google),
        iconResId = R.drawable.ic_google_logo,
        contentDescription = stringResource(R.string.content_desc_google_sign_in_button),
        onClick = {
            coroutineScope.launch {
                try {
                    val credential = GoogleAuthProvider.getGoogleCredential(
                        context = context,
                        clientId = clientId,
                        intent = intent,
                        onNoAccountFound = { addAccountIntent -> launcher?.launch(addAccountIntent) }
                    )
                    onCredentialReceived(credential)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        },
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        loadingText = loadingText
    )
}
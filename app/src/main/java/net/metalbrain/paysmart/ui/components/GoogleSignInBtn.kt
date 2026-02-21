package net.metalbrain.paysmart.ui.components

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthProvider

@Composable
fun GoogleSignInBtn(
    modifier: Modifier = Modifier,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait...",
    onCredentialReceived: (AuthCredential) -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            coroutineScope.launch {
                try {
                    val credential = GoogleAuthProvider.getGoogleCredential(context, launcher)
                    onCredentialReceived(credential)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = loadingText)
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google sign-in button",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.continue_with_google))
        }
    }
}

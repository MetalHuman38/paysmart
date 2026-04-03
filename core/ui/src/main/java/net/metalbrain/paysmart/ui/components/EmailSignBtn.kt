package net.metalbrain.paysmart.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R

@Composable
fun EmailSignInBtn(
    email: String,
    onLinkSent: () -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait..."
) {
    AuthProviderButton(
        text = stringResource(R.string.continue_with_email),
        iconResId = R.drawable.ic_email_logo,
        contentDescription = stringResource(R.string.content_desc_email_sign_in_button),
        onClick = {
            if (email.isBlank()) {
                onError(IllegalArgumentException("Email cannot be empty"))
                return@AuthProviderButton
            }
            onLinkSent()
        },
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        loadingText = loadingText
    )
}

package net.metalbrain.paysmart.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R


@Composable
fun FacebookSignInButton(
    activity: Activity,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait...",
    onClick: () -> Unit,
) {
    AuthProviderButton(
        text = stringResource(R.string.continue_with_facebook),
        iconResId = R.drawable.ic_facebook_logo,
        contentDescription = stringResource(R.string.content_desc_facebook_sign_in_button),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        loadingText = loadingText
    )
}

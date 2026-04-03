package net.metalbrain.paysmart.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R

@Composable
fun PasskeySignBtn(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Please wait...",
    onClick: () -> Unit
) {
    AuthProviderButton(
        text = stringResource(R.string.continue_with_passkey),
        iconResId = R.drawable.ic_password,
        contentDescription = stringResource(R.string.continue_with_passkey),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        loadingText = loadingText
    )
}

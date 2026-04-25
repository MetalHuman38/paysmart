package net.metalbrain.paysmart.core.features.account.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R as CoreUiR
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.AuthProviderButton

@Composable
fun EmailVerificationBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthProviderButton(
        text = stringResource(R.string.continue_with_email),
        iconResId = CoreUiR.drawable.ic_email_logo,
        contentDescription = stringResource(R.string.content_desc_verify_email_button),
        onClick = onClick,
        modifier = modifier,
    )
}
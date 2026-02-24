package net.metalbrain.paysmart.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HaveAnAccount(
    modifier: Modifier = Modifier,
    onSignInClicked: () -> Unit,
) {
    AccountSwitchPrompt(
        variant = AccountSwitchVariant.HAVE_ACCOUNT,
        modifier = modifier,
        onActionClick = onSignInClicked,
    )
}

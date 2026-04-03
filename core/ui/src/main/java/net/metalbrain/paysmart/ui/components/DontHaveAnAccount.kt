package net.metalbrain.paysmart.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DontHaveAnAccount(
    modifier: Modifier = Modifier,
    onSignUpClicked: () -> Unit,
) {
    AccountSwitchPrompt(
        variant = AccountSwitchVariant.DONT_HAVE_ACCOUNT,
        modifier = modifier,
        onActionClick = onSignUpClicked,
    )
}

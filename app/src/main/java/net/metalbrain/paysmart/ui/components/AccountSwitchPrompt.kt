package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R

enum class AccountSwitchVariant {
    HAVE_ACCOUNT,
    DONT_HAVE_ACCOUNT,
}

@Composable
fun AccountSwitchPrompt(
    variant: AccountSwitchVariant,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit,
) {
    val promptRes = when (variant) {
        AccountSwitchVariant.HAVE_ACCOUNT -> R.string.have_an_account
        AccountSwitchVariant.DONT_HAVE_ACCOUNT -> R.string.dont_have_account
    }
    val actionRes = when (variant) {
        AccountSwitchVariant.HAVE_ACCOUNT -> R.string.sign_in
        AccountSwitchVariant.DONT_HAVE_ACCOUNT -> R.string.sign_up
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(promptRes),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(actionRes),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
            ),
            modifier = Modifier.clickable(onClick = onActionClick),
        )
    }
}

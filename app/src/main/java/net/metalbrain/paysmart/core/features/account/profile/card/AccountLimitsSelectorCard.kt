package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.components.AccountSelectorRow
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitSelectorRowUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountLimitsSelectorCard(
    modifier: Modifier = Modifier,
    accounts: List<AccountLimitSelectorRowUiState>,
    onAccountClick: (String) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        tonalElevation = Dimens.sm,
        shadowElevation = 0.dp
    ) {

        Column(
            modifier = Modifier.padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            if (accounts.isEmpty()) {
                Text(
                    text = stringResource(R.string.account_limits_empty_accounts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                accounts.forEach { account ->
                    AccountSelectorRow(
                        flagEmoji = account.flagEmoji,
                        currencyName = account.currencyName,
                        accountDetails = account.accountDescriptor,
                        onClick = { onAccountClick(account.currencyCode) }
                    )
                }
            }
        }
    }
}

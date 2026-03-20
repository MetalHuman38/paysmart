package net.metalbrain.paysmart.core.features.capabilities.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.account.profile.components.AccountLimitFlagChip
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountSelectorRow(
    flagEmoji: String,
    currencyName: String,
    accountDetails: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Dimens.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AccountLimitFlagChip(
            targetFlag = flagEmoji,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(Dimens.xs))

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Text(
                text = currencyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = accountDetails,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

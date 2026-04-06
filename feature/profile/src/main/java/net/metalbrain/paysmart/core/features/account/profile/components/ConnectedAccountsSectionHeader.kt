package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsProviderPill
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ConnectedAccountsSectionHeader(
    tab: ConnectedAccountsTab
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(tab.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            ConnectedAccountsProviderPill(
                label = stringResource(tab.providerRes)
            )
        }
        Text(
            text = stringResource(tab.supportingRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

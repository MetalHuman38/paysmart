package net.metalbrain.paysmart.core.features.account.profile.components

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
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsStateCard
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun CardsTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        ConnectedAccountsSectionHeader(
            tab = ConnectedAccountsTab.CARDS
        )

        ConnectedAccountsStateCard(
            animationRes = R.raw.card,
            title = stringResource(R.string.profile_connected_accounts_cards_empty_title),
            supporting = stringResource(R.string.profile_connected_accounts_cards_empty_supporting)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                Text(
                    text = stringResource(R.string.profile_connected_accounts_cards_future_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_connected_accounts_cards_future_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

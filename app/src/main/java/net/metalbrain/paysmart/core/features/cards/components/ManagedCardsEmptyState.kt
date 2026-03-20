package net.metalbrain.paysmart.core.features.cards.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsStateCard

@Composable
fun ManagedCardsEmptyState(
    onRefresh: (() -> Unit)? = null
) {
    ConnectedAccountsStateCard(
        animationRes = R.raw.card,
        title = stringResource(R.string.profile_connected_accounts_cards_empty_title),
        supporting = stringResource(R.string.profile_connected_accounts_cards_empty_supporting),
        actionText = if (onRefresh != null) {
            stringResource(R.string.profile_connected_accounts_cards_refresh_action)
        } else {
            null
        },
        onAction = onRefresh
    )
}

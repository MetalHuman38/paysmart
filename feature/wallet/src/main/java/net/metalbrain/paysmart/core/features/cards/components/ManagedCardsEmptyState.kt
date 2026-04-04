package net.metalbrain.paysmart.core.features.cards.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.ui.R as CoreUiR
import net.metalbrain.paysmart.feature.wallet.R
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsStateCard

@Composable
fun ManagedCardsEmptyState(
    onRefresh: (() -> Unit)? = null
) {
    ConnectedAccountsStateCard(
        animationRes = CoreUiR.raw.card,
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

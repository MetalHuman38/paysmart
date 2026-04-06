package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsStateCard
import net.metalbrain.paysmart.core.features.cards.components.ManagedCardsEmptyState
import net.metalbrain.paysmart.core.features.cards.components.ManagedCardsList
import net.metalbrain.paysmart.core.features.cards.state.ManagedCardsScreenPhase
import net.metalbrain.paysmart.core.features.cards.state.ManagedCardsUiState
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun CardsTabContent(
    state: ManagedCardsUiState,
    onRefresh: () -> Unit,
    onSetDefaultCard: (String) -> Unit,
    onRemoveCard: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        ConnectedAccountsSectionHeader(
            tab = ConnectedAccountsTab.CARDS
        )

        when (state.phase) {
            ManagedCardsScreenPhase.LOADING -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.card,
                    title = stringResource(R.string.profile_connected_accounts_cards_loading_title),
                    supporting = stringResource(R.string.profile_connected_accounts_cards_loading_supporting)
                )
            }

            ManagedCardsScreenPhase.EMPTY -> {
                ManagedCardsEmptyState(onRefresh = onRefresh)
            }

            ManagedCardsScreenPhase.ERROR -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.card,
                    title = stringResource(R.string.profile_connected_accounts_cards_error_title),
                    supporting = stringResource(R.string.profile_connected_accounts_cards_error_supporting),
                    actionText = stringResource(R.string.profile_connected_accounts_cards_refresh_action),
                    onAction = onRefresh
                )
            }

            ManagedCardsScreenPhase.READY -> {
                ManagedCardsList(
                    cards = state.cards,
                    activeCardActionId = state.activeCardActionId,
                    onSetDefault = onSetDefaultCard,
                    onRemove = onRemoveCard
                )
            }
        }
    }
}

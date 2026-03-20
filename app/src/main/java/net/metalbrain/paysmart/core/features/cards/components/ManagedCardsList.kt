package net.metalbrain.paysmart.core.features.cards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ManagedCardsList(
    cards: List<ManagedCardData>,
    activeCardActionId: String?,
    onSetDefault: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        cards.forEach { card ->
            ManagedCardRow(
                card = card,
                isActionInProgress = activeCardActionId == card.id,
                onSetDefault = { onSetDefault(card.id) },
                onRemove = { onRemove(card.id) }
            )
        }
    }
}

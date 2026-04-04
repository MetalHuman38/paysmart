package net.metalbrain.paysmart.core.features.cards.state

import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardErrorCode

data class ManagedCardsUiState(
    val cards: List<ManagedCardData> = emptyList(),
    val phase: ManagedCardsScreenPhase = ManagedCardsScreenPhase.LOADING,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val activeCardActionId: String? = null,
    val lastErrorCode: ManagedCardErrorCode? = null
)

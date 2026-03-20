package net.metalbrain.paysmart.core.features.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardErrorCode
import net.metalbrain.paysmart.core.features.cards.repository.ManagedCardsApiException
import net.metalbrain.paysmart.core.features.cards.repository.ManagedCardsGateway
import net.metalbrain.paysmart.core.features.cards.state.ManagedCardsScreenPhase
import net.metalbrain.paysmart.core.features.cards.state.ManagedCardsUiState
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ManagedCardsViewModel @Inject constructor(
    private val managedCardsGateway: ManagedCardsGateway,
    private val userManager: UserManager
) : ViewModel() {

    private val syncState = MutableStateFlow(ManagedCardsSyncState())

    private val currentCards = managedCardsGateway.observeCurrent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState = combine(currentCards, syncState) { cards, sync ->
        ManagedCardsUiState(
            cards = cards,
            phase = derivePhase(cards, sync),
            isInitialLoading = sync.isInitialLoading,
            isRefreshing = sync.isRefreshing,
            activeCardActionId = sync.activeCardActionId,
            lastErrorCode = sync.lastErrorCode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ManagedCardsUiState()
    )

    init {
        viewModelScope.launch {
            userManager.authState.collect { authState ->
                if (authState is AuthState.Authenticated) {
                    sync(isInitial = true)
                } else {
                    syncState.value = ManagedCardsSyncState(isInitialLoading = false)
                }
            }
        }
    }

    fun refresh() {
        sync(isInitial = false)
    }

    fun removeCard(paymentMethodId: String) {
        runCardAction(paymentMethodId) {
            managedCardsGateway.removeCard(paymentMethodId)
        }
    }

    fun setDefaultCard(paymentMethodId: String) {
        runCardAction(paymentMethodId) {
            managedCardsGateway.setDefaultCard(paymentMethodId)
        }
    }

    private fun sync(isInitial: Boolean) {
        viewModelScope.launch {
            syncState.update { current ->
                current.copy(
                    isInitialLoading = isInitial && currentCards.value.isEmpty(),
                    isRefreshing = !isInitial,
                    activeCardActionId = null,
                    lastErrorCode = null
                )
            }

            managedCardsGateway.syncFromServer().fold(
                onSuccess = {
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            activeCardActionId = null,
                            lastErrorCode = null
                        )
                    }
                },
                onFailure = { throwable ->
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            activeCardActionId = null,
                            lastErrorCode = throwable.toManagedCardErrorCode()
                        )
                    }
                }
            )
        }
    }

    private fun runCardAction(
        paymentMethodId: String,
        action: suspend () -> Result<List<ManagedCardData>>
    ) {
        viewModelScope.launch {
            syncState.update {
                it.copy(
                    isInitialLoading = false,
                    isRefreshing = false,
                    activeCardActionId = paymentMethodId,
                    lastErrorCode = null
                )
            }

            action().fold(
                onSuccess = {
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            activeCardActionId = null,
                            lastErrorCode = null
                        )
                    }
                },
                onFailure = { throwable ->
                    syncState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            activeCardActionId = null,
                            lastErrorCode = throwable.toManagedCardErrorCode()
                        )
                    }
                }
            )
        }
    }
}

private fun derivePhase(
    cards: List<ManagedCardData>,
    sync: ManagedCardsSyncState
): ManagedCardsScreenPhase {
    if (sync.isInitialLoading && cards.isEmpty()) {
        return ManagedCardsScreenPhase.LOADING
    }
    if (sync.lastErrorCode != null && cards.isEmpty()) {
        return ManagedCardsScreenPhase.ERROR
    }
    return if (cards.isEmpty()) {
        ManagedCardsScreenPhase.EMPTY
    } else {
        ManagedCardsScreenPhase.READY
    }
}

private fun Throwable.toManagedCardErrorCode(): ManagedCardErrorCode {
    return (this as? ManagedCardsApiException)?.code
        ?: ManagedCardErrorCode.STRIPE_MANAGED_CARD_ACTION_FAILED
}

private data class ManagedCardsSyncState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val activeCardActionId: String? = null,
    val lastErrorCode: ManagedCardErrorCode? = null
)

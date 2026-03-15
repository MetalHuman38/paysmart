package net.metalbrain.paysmart.core.features.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.core.features.transactions.state.TransactionDetailUiState
import net.metalbrain.paysmart.data.repository.TransactionHistoryRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModel @Inject constructor(
    repository: TransactionHistoryRepository
) : ViewModel() {

    private val selectedTransactionId = MutableStateFlow<String?>(null)

    val uiState = selectedTransactionId
        .flatMapLatest { transactionId ->
            if (transactionId.isNullOrBlank()) {
                flowOf(TransactionDetailUiState(isLoading = true))
            } else {
                repository.observeTransaction(transactionId).map { transaction ->
                    TransactionDetailUiState(
                        isLoading = false,
                        transaction = transaction
                    )
                }
            }
        }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionDetailUiState()
        )

    fun load(transactionId: String) {
        val normalized = transactionId.trim()
        if (normalized.isNotEmpty() && selectedTransactionId.value != normalized) {
            selectedTransactionId.value = normalized
        }
    }
}

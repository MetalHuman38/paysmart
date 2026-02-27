package net.metalbrain.paysmart.core.features.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.data.repository.TransactionRepository

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatus: Set<String> get() = _statusFilter.value

    private val _currencyFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedCurrencies: Set<String> get() = _currencyFilter.value

    val filteredTransactions = combine(
        repository.observeTransactions(),
        _statusFilter,
        _currencyFilter
    ) { transactions, status, currency ->
        transactions.filter {
            (status.isEmpty() || it.status in status) &&
                    (currency.isEmpty() || it.currency in currency)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStatusFilter(newStatus: Set<String>) {
        _statusFilter.value = newStatus
    }

    fun setCurrencyFilter(newCurrency: Set<String>) {
        _currencyFilter.value = newCurrency
    }

    fun clearFilters() {
        _statusFilter.value = emptySet()
        _currencyFilter.value = emptySet()
    }
}

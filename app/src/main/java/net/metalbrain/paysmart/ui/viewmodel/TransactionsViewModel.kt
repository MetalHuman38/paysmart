package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.domain.model.Transaction


@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatus: Set<String> get() = _statusFilter.value

    private val _currencyFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedCurrencies: Set<String> get() = _currencyFilter.value

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    val filteredTransactions = combine(
        _allTransactions, _statusFilter, _currencyFilter
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

    init {
        // Load transactions on launch
        viewModelScope.launch {
            val result = repository.getTransactions()
            _allTransactions.value = result
        }
    }
}

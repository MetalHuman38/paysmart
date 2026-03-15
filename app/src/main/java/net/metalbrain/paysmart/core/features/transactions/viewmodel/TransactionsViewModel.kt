package net.metalbrain.paysmart.core.features.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.data.repository.TransactionRepository

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {

    private val allTransactions = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val statusFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatus: Set<String> get() = statusFilter.value

    private val currencyFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedCurrencies: Set<String> get() = currencyFilter.value

    val availableStatuses = allTransactions
        .map { transactions -> transactions.map { it.status }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCurrencies = allTransactions
        .map { transactions -> transactions.map { it.currency }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions = combine(
        allTransactions,
        statusFilter,
        currencyFilter
    ) { transactions, statuses, currencies ->
        transactions.filter {
            (statuses.isEmpty() || it.status in statuses) &&
                (currencies.isEmpty() || it.currency in currencies)
        }.sortedByDescending { transaction -> transaction.createdAtMs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStatusFilter(newStatus: Set<String>) {
        statusFilter.value = newStatus
    }

    fun setCurrencyFilter(newCurrency: Set<String>) {
        currencyFilter.value = newCurrency
    }

    fun clearFilters() {
        statusFilter.value = emptySet()
        currencyFilter.value = emptySet()
    }
}

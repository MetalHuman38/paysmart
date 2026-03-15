package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import net.metalbrain.paysmart.data.repository.TransactionHistoryRepository

@HiltViewModel
class AccountStatementViewModel @Inject constructor(
    historyRepository: TransactionHistoryRepository
) : ViewModel() {

    val transactions = historyRepository
        .pagedTransactions(pageSize = 30)
        .cachedIn(viewModelScope)
}

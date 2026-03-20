package net.metalbrain.paysmart.core.features.help.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.data.repository.TransactionRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.model.Transaction
import java.util.Locale

data class HelpUiState(
    val displayName: String = "",
    val recentTransfers: List<Transaction> = emptyList(),
    val recentFundings: List<Transaction> = emptyList()
) {
    val firstName: String
        get() = displayName.trim()
            .split(Regex("\\s+"))
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
            .orEmpty()
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HelpViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    userManager: UserManager,
    profileCacheRepository: UserProfileCacheRepository
) : ViewModel() {

    private val profile = userManager.authState.flatMapLatest { auth ->
        when (auth) {
            is AuthState.Authenticated -> profileCacheRepository.observeByUid(auth.uid)
            else -> flowOf(null)
        }
    }

    val uiState = combine(
        profile,
        transactionRepository.observeTransactions()
    ) { profile, transactions ->
        val sortedTransactions = transactions.sortedByDescending { transaction ->
            transaction.createdAtMs
        }
        val recentFundings = sortedTransactions
            .filter(Transaction::isFundingSupportCandidate)
            .take(8)
        val recentTransfers = sortedTransactions
            .filter(Transaction::isTransferSupportCandidate)
            .ifEmpty {
                sortedTransactions.filterNot(Transaction::isFundingSupportCandidate)
            }
            .take(8)

        HelpUiState(
            displayName = profile?.displayName?.trim().orEmpty(),
            recentTransfers = recentTransfers,
            recentFundings = recentFundings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HelpUiState()
    )
}

private fun Transaction.isFundingSupportCandidate(): Boolean {
    val normalizedTitle = title.trim().lowercase(Locale.US)
    return normalizedTitle.contains("top up") ||
        normalizedTitle.contains("wallet funding") ||
        normalizedTitle.contains("card funding") ||
        normalizedTitle.contains("add money") ||
        iconRes == R.drawable.ic_topup_bank ||
        iconRes == R.drawable.ic_topup_mastercard
}

private fun Transaction.isTransferSupportCandidate(): Boolean {
    if (isFundingSupportCandidate()) {
        return false
    }

    val normalizedTitle = title.trim().lowercase(Locale.US)
    return normalizedTitle.contains("send") ||
        normalizedTitle.contains("transfer") ||
        normalizedTitle.contains("paid") ||
        iconRes == R.drawable.ic_send
}

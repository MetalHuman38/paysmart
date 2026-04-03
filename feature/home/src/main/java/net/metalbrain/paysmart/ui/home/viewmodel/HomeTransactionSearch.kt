package net.metalbrain.paysmart.ui.home.viewmodel

import java.util.Locale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter

data class HomeTransactionSearchSnapshot(
    val visibleTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val availableProviders: List<HomeTransactionProviderFilter> = emptyList(),
    val selectedProviders: Set<HomeTransactionProviderFilter> = emptySet(),
)

@OptIn(FlowPreview::class)
fun observeHomeTransactionSearch(
    transactions: Flow<List<Transaction>>,
    searchQuery: Flow<String>,
    selectedProviders: Flow<Set<HomeTransactionProviderFilter>>,
    defaultLimit: Int = 3,
    searchLimit: Int = 5,
    debounceMs: Long = HOME_TRANSACTION_SEARCH_DEBOUNCE_MS,
): Flow<HomeTransactionSearchSnapshot> {
    val debouncedQuery = searchQuery.debounce(debounceMs)
    return combine(
        transactions,
        searchQuery,
        debouncedQuery,
        selectedProviders,
    ) { allTransactions, rawQuery, settledQuery, selectedProviderSet ->
        val sortedTransactions = allTransactions.sortedByDescending(Transaction::createdAtMs)
        val availableProviders = HomeTransactionProviderFilter.entries.filter { provider ->
            sortedTransactions.any(provider::matches)
        }
        val normalizedSelectedProviders = selectedProviderSet.intersect(availableProviders.toSet())
        val activeSearch = rawQuery.isNotBlank() || normalizedSelectedProviders.isNotEmpty()
        val filteredTransactions = sortedTransactions.filter { transaction ->
            transaction.matchesSearch(
                query = settledQuery,
                selectedProviders = normalizedSelectedProviders,
            )
        }
        HomeTransactionSearchSnapshot(
            visibleTransactions = if (activeSearch) {
                filteredTransactions.take(searchLimit)
            } else {
                sortedTransactions.take(defaultLimit)
            },
            searchQuery = rawQuery,
            isSearchActive = activeSearch,
            availableProviders = availableProviders,
            selectedProviders = normalizedSelectedProviders,
        )
    }
}

private fun Transaction.matchesSearch(
    query: String,
    selectedProviders: Set<HomeTransactionProviderFilter>,
): Boolean {
    if (selectedProviders.isNotEmpty() && selectedProviders.none { provider -> provider.matches(this) }) {
        return false
    }

    val normalizedQuery = query.trim().lowercase(Locale.US)
    if (normalizedQuery.isBlank()) {
        return true
    }

    val amountTokens = buildList {
        add(String.format(Locale.US, "%.2f", amount))
        add(String.format(Locale.US, "%.0f", amount))
    }
    val searchableFields = listOfNotNull(
        title,
        status,
        currency,
        provider,
        paymentRail,
        reference,
        externalReference,
    ).map { value -> value.lowercase(Locale.US) }

    return searchableFields.any { value -> value.contains(normalizedQuery) } ||
        amountTokens.any { token -> token.contains(normalizedQuery) }
}

private fun HomeTransactionProviderFilter.matches(transaction: Transaction): Boolean {
    val providerName = transaction.provider
        ?.trim()
        ?.lowercase(Locale.US)
        .orEmpty()
    return providerName.contains(providerKey)
}

private val HomeTransactionProviderFilter.providerKey: String
    get() = when (this) {
        HomeTransactionProviderFilter.STRIPE -> "stripe"
        HomeTransactionProviderFilter.FLUTTERWAVE -> "flutterwave"
    }

private const val HOME_TRANSACTION_SEARCH_DEBOUNCE_MS = 200L

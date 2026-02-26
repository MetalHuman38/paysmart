package net.metalbrain.paysmart.core.features.transactions.components

sealed class TransactionFilter {
    object All : TransactionFilter()
    object Status : TransactionFilter()
    object Currency : TransactionFilter()
}

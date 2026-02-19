package net.metalbrain.paysmart.ui.transactions.components

sealed class TransactionFilter {
    object All : TransactionFilter()
    object Status : TransactionFilter()
    object Currency : TransactionFilter()
}

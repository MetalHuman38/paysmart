package net.metalbrain.paysmart.ui.home

sealed class TransactionFilter {
    object All : TransactionFilter()
    object Status : TransactionFilter()
    object Currency : TransactionFilter()
}

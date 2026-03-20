package net.metalbrain.paysmart.domain.model


data class TransactionHistoryQuery(
    val statuses: Set<String> = emptySet(),
    val currencies: Set<String> = emptySet(),
    val fromCreatedAtMs: Long? = null,
    val toCreatedAtMs: Long? = null
)

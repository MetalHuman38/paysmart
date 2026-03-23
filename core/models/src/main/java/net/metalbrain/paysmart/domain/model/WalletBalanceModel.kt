package net.metalbrain.paysmart.domain.model

data class WalletBalanceModel(
    val userId: String,
    val balancesByCurrency: Map<String, Double> = emptyMap(),
    val rewardsPoints: Double = 0.0,
    val updatedAtMs: Long = System.currentTimeMillis()
)

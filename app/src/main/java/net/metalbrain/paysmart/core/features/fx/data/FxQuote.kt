package net.metalbrain.paysmart.core.features.fx.data

data class FxQuote(
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val rate: Double,
    val recipientAmount: Double,
    val fees: List<FxFeeLine>,
    val guaranteeSeconds: Int,
    val arrivalSeconds: Int,
    val rateSource: String,
    val updatedAtMs: Long
)

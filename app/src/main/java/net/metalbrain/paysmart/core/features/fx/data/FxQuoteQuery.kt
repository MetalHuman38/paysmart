package net.metalbrain.paysmart.core.features.fx.data

data class FxQuoteQuery(
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val method: FxPaymentMethod
)

package net.metalbrain.paysmart.core.features.fx.data

data class FxFeeLine(
    val label: String,
    val amount: Double,
    val code: String? = null
)

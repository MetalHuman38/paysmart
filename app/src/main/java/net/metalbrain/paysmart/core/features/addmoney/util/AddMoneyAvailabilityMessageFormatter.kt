package net.metalbrain.paysmart.core.features.addmoney.util

internal fun addMoneyUnavailableMessage(countryName: String): String {
    val normalized = countryName.trim().ifBlank { "this market" }
    return "Add money is not available in $normalized yet."
}

internal fun addMoneyUnavailableSupportingText(countryName: String): String {
    val normalized = countryName.trim().ifBlank { "this market" }
    return "We will enable top ups in $normalized once a supported funding provider is live for that market."
}

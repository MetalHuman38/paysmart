package net.metalbrain.paysmart.core.features.capabilities.catalog

data class AccountLimitMarketProfile(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val currencyName: String,
    val currencySymbol: String,
    val supportsIban: Boolean,
    val supportsLocalAccount: Boolean
)

package net.metalbrain.paysmart.core.features.capabilities.catalog

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod

data class CountryCapabilityProfile(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val addMoney: AddMoneyMarketPolicy,
    val capabilities: List<CapabilityItem>
) {
    val addMoneyProviders: List<AddMoneyProvider>
        get() = addMoney.providers

    val addMoneyMethods: List<FxPaymentMethod>
        get() = addMoney.methods

    val isAddMoneySupported: Boolean
        get() = addMoney.isSupported
}

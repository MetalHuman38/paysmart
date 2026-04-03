package net.metalbrain.paysmart.core.features.capabilities.catalog

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod

object CountryCapabilityCatalog {
    const val DEFAULT_ISO2: String = "GB"

    fun defaultProfile(): CountryCapabilityProfile {
        return CountryCapabilityProfile(
            iso2 = DEFAULT_ISO2,
            countryName = "United Kingdom",
            flagEmoji = "\uD83C\uDDEC\uD83C\uDDE7",
            currencyCode = "GBP",
            addMoney = AddMoneyMarketPolicy(
                providers = listOf(AddMoneyProvider.STRIPE),
                methods = listOf(FxPaymentMethod.DEBIT_CARD, FxPaymentMethod.CREDIT_CARD)
            ),
            capabilities = listOf(
                CapabilityItem(
                    key = CapabilityKey.SEND_INTERNATIONAL,
                    title = "Send money abroad",
                    subtitle = "Low-cost international transfers."
                )
            )
        )
    }

    fun topUpPolicyHint(profile: CountryCapabilityProfile): String {
        return if (!profile.isAddMoneySupported) {
            "Add money is not available in ${profile.countryName} yet."
        } else if (profile.addMoneyMethods.contains(FxPaymentMethod.ACCOUNT_TRANSFER)) {
            "Card and bank transfer top ups are available for ${profile.iso2}."
        } else {
            "Card top up is currently available for ${profile.iso2}."
        }
    }
}

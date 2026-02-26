package net.metalbrain.paysmart.core.features.capabilities.catalog

import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod

enum class CapabilityKey {
    SEND_INTERNATIONAL,
    CARD_SPEND_ABROAD,
    HOLD_AND_CONVERT,
    RECEIVE_MONEY,
    EARN_RETURN;

    companion object {
        fun fromRaw(raw: String?): CapabilityKey {
            return when (raw?.trim()?.lowercase()) {
                "send_international" -> SEND_INTERNATIONAL
                "card_spend_abroad" -> CARD_SPEND_ABROAD
                "hold_and_convert" -> HOLD_AND_CONVERT
                "receive_money" -> RECEIVE_MONEY
                "earn_return" -> EARN_RETURN
                else -> SEND_INTERNATIONAL
            }
        }
    }
}

data class CapabilityItem(
    val key: CapabilityKey,
    val title: String,
    val subtitle: String,
    val footnote: String? = null
)

data class CountryCapabilityProfile(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val addMoneyMethods: List<FxPaymentMethod>,
    val capabilities: List<CapabilityItem>
)

object CountryCapabilityCatalog {
    const val DEFAULT_ISO2: String = "GB"

    fun defaultProfile(): CountryCapabilityProfile {
        return CountryCapabilityProfile(
            iso2 = DEFAULT_ISO2,
            countryName = "United Kingdom",
            flagEmoji = "\uD83C\uDDEC\uD83C\uDDE7",
            currencyCode = "GBP",
            addMoneyMethods = listOf(FxPaymentMethod.DEBIT_CARD, FxPaymentMethod.CREDIT_CARD),
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
        return if (profile.addMoneyMethods.contains(FxPaymentMethod.ACCOUNT_TRANSFER)) {
            "Card and bank transfer top ups are available for ${profile.iso2}."
        } else {
            "Card top up is currently available for ${profile.iso2}."
        }
    }
}

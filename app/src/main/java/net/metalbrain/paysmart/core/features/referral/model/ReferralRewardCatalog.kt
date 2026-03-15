package net.metalbrain.paysmart.core.features.referral.model

import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import java.util.Locale

data class ReferralReward(
    val amount: Int,
    val currencyCode: String
)

object ReferralRewardCatalog {
    private const val DEFAULT_REWARD_AMOUNT = 10

    // Market-specific reward overrides can be added here without changing UI resources.
    private val rewardAmountsByCountryIso2: Map<String, Int> = mapOf(
        CountryCapabilityCatalog.DEFAULT_ISO2 to DEFAULT_REWARD_AMOUNT
    )

    fun rewardFor(countryIso2: String, currencyCode: String): ReferralReward {
        val normalizedIso2 = countryIso2
            .trim()
            .uppercase(Locale.US)
            .ifBlank { CountryCapabilityCatalog.DEFAULT_ISO2 }
        val normalizedCurrencyCode = currencyCode
            .trim()
            .uppercase(Locale.US)
            .ifBlank { CountryCapabilityCatalog.defaultProfile().currencyCode }

        return ReferralReward(
            amount = rewardAmountsByCountryIso2[normalizedIso2] ?: DEFAULT_REWARD_AMOUNT,
            currencyCode = normalizedCurrencyCode
        )
    }
}

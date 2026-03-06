package net.metalbrain.paysmart.ui.capabilities

import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CountryCapabilityCatalog], ensuring correct retrieval of default profiles,
 * proper mapping of capability keys, and accurate generation of top-up policy hints based
 * on available payment methods.
 */
class CountryCapabilityCatalogTest {

    @Test
    fun `default profile is GB with card methods`() {
        val profile = CountryCapabilityCatalog.defaultProfile()

        assertEquals("GB", profile.iso2)
        assertEquals("GBP", profile.currencyCode)
        assertTrue(profile.addMoneyMethods.contains(FxPaymentMethod.DEBIT_CARD))
        assertTrue(profile.addMoneyMethods.contains(FxPaymentMethod.CREDIT_CARD))
    }

    @Test
    fun `capability key parsing maps known and unknown values`() {
        assertEquals(
            CapabilityKey.CARD_SPEND_ABROAD,
            CapabilityKey.fromRaw("card_spend_abroad")
        )
        assertEquals(
            CapabilityKey.SEND_INTERNATIONAL,
            CapabilityKey.fromRaw("unknown_key")
        )
    }

    @Test
    fun `top up policy hint reflects account transfer availability`() {
        val cardOnly = CountryCapabilityProfile(
            iso2 = "NG",
            countryName = "Nigeria",
            flagEmoji = "🇳🇬",
            currencyCode = "NGN",
            addMoneyMethods = listOf(FxPaymentMethod.DEBIT_CARD, FxPaymentMethod.CREDIT_CARD),
            capabilities = emptyList()
        )
        val cardAndBank = cardOnly.copy(
            iso2 = "GB",
            countryName = "United Kingdom",
            currencyCode = "GBP",
            addMoneyMethods = listOf(
                FxPaymentMethod.DEBIT_CARD,
                FxPaymentMethod.CREDIT_CARD,
                FxPaymentMethod.ACCOUNT_TRANSFER
            )
        )

        assertTrue(CountryCapabilityCatalog.topUpPolicyHint(cardOnly).contains("Card top up"))
        assertTrue(CountryCapabilityCatalog.topUpPolicyHint(cardAndBank).contains("bank transfer"))
    }
}

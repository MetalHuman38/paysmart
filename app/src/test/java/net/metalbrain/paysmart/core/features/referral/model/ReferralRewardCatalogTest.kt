package net.metalbrain.paysmart.core.features.referral.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferralRewardCatalogTest {

    @Test
    fun `reward uses market currency with default amount`() {
        val reward = ReferralRewardCatalog.rewardFor(
            countryIso2 = "US",
            currencyCode = "usd"
        )

        assertEquals(10, reward.amount)
        assertEquals("USD", reward.currencyCode)
    }

    @Test
    fun `blank currency falls back to default market currency`() {
        val reward = ReferralRewardCatalog.rewardFor(
            countryIso2 = "",
            currencyCode = ""
        )

        assertEquals(10, reward.amount)
        assertEquals("GBP", reward.currencyCode)
    }
}

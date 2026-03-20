package net.metalbrain.paysmart.core.features.capabilities.mapper

import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitMarketProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionTemplate
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionType
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitTabSpec
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitValueProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountLimitDetailsUiMapperTest {

    private val profile = AccountLimitProfile(
        iso2 = "GB",
        countryName = "United Kingdom",
        flagEmoji = "🇬🇧",
        currencyCode = "GBP",
        tabs = listOf(
            AccountLimitTabSpec(AccountLimitKey.SEND, "Limit on send"),
            AccountLimitTabSpec(AccountLimitKey.RECEIVE, "Limit on receive")
        ),
        sectionTemplates = listOf(
            AccountLimitSectionTemplate(
                key = "single",
                titleTemplate = "Send Limit (Single transaction)",
                type = AccountLimitSectionType.SINGLE
            ),
            AccountLimitSectionTemplate(
                key = "daily",
                titleTemplate = "Daily limit of {amount}",
                type = AccountLimitSectionType.PERIOD
            )
        )
    )

    private val market = AccountLimitMarketProfile(
        iso2 = "GB",
        countryName = "United Kingdom",
        flagEmoji = "🇬🇧",
        currencyCode = "GBP",
        currencyName = "Pound sterling",
        currencySymbol = "£",
        supportsIban = true,
        supportsLocalAccount = true
    )

    private val values = AccountLimitValueProfile(
        iso2 = "GB",
        send = mapOf("single" to 10_000.0, "daily" to 10_000.0),
        receive = mapOf("single" to 8_000.0, "daily" to 12_000.0)
    )

    @Test
    fun `buildCards formats send amounts and v1 usage defaults`() {
        val cards = AccountLimitDetailsUiMapper.buildCards(
            profile = profile,
            marketProfile = market,
            selectedTab = AccountLimitKey.SEND,
            valueProfile = values,
            spentOfLimitFormat = { spent, limit -> "$spent spent / $limit" },
            leftFormat = { limit -> "$limit left" }
        )

        assertEquals("Send Limit (Single transaction)", cards[0].title)
        assertEquals("£10,000.00", cards[0].leadingLabel)
        assertEquals("£10,000.00", cards[0].trailingLabel)
        assertEquals("Daily limit of £10,000.00", cards[1].title)
        assertEquals("£0.00 spent / £10,000.00", cards[1].leadingLabel)
        assertEquals("£10,000.00 left", cards[1].trailingLabel)
        assertEquals(0f, cards[1].progress, 0f)
    }

    @Test
    fun `buildCards switches single transaction wording for receive tab`() {
        val cards = AccountLimitDetailsUiMapper.buildCards(
            profile = profile,
            marketProfile = market,
            selectedTab = AccountLimitKey.RECEIVE,
            valueProfile = values,
            spentOfLimitFormat = { spent, limit -> "$spent spent / $limit" },
            leftFormat = { limit -> "$limit left" }
        )

        assertEquals("Receive Limit (Single transaction)", cards[0].title)
        assertEquals("£8,000.00", cards[0].leadingLabel)
        assertEquals("Daily limit of £12,000.00", cards[1].title)
        assertEquals("£12,000.00 left", cards[1].trailingLabel)
    }
}

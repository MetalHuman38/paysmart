package net.metalbrain.paysmart.core.features.capabilities.catalog

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountTypeAndLimitCatalogTest {

    @Test
    fun parserReadsMarketCapabilitiesAndResolutionPrefersRequestedHomeMarket() {
        val entries = AccountTypeAndLimitJsonParser.parse(
            """
            [
              {
                "iso2": "DE",
                "name": "Germany",
                "flag": "🇩🇪",
                "currency": { "code": "EUR", "name": "Euro", "symbol": "€" },
                "payoutRules": { "bankTransfer": { "supportsIban": true } }
              },
              {
                "iso2": "ES",
                "name": "Spain",
                "flag": "🇪🇸",
                "currency": { "code": "EUR", "name": "Euro", "symbol": "€" },
                "payoutRules": {
                  "bankTransfer": {
                    "supportsIban": true,
                    "local": { "accountNumber": { "lengths": [10] } }
                  }
                }
              },
              {
                "iso2": "GB",
                "name": "United Kingdom",
                "flag": "🇬🇧",
                "currency": { "code": "GBP", "name": "Pound sterling", "symbol": "£" },
                "payoutRules": {
                  "bankTransfer": {
                    "supportsIban": true,
                    "local": { "accountNumber": { "lengths": [8] } }
                  }
                }
              }
            ]
            """.trimIndent()
        )

        val spanishEur = AccountTypeAndLimitCatalog.resolveMarketForCurrency(
            entries = entries,
            rawCurrencyCode = "eur",
            preferredIso2 = "ES"
        )
        val fallbackEur = AccountTypeAndLimitCatalog.resolveMarketForCurrency(
            entries = entries,
            rawCurrencyCode = "EUR",
            preferredIso2 = "GB"
        )

        assertEquals(3, entries.size)
        assertNotNull(spanishEur)
        assertEquals("ES", spanishEur?.iso2)
        assertEquals("🇪🇸", spanishEur?.flagEmoji)
        assertTrue(spanishEur?.supportsIban == true)
        assertTrue(spanishEur?.supportsLocalAccount == true)

        assertNotNull(fallbackEur)
        assertEquals("DE", fallbackEur?.iso2)
        assertTrue(fallbackEur?.supportsIban == true)
        assertFalse(fallbackEur?.supportsLocalAccount == true)
    }
}

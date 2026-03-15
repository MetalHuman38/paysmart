package net.metalbrain.paysmart.core.features.language.data

import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDisplayCatalogTest {

    @Test
    fun `unknown language falls back to catalog default country`() {
        val display = resolveLanguageDisplaySpec("unknown")

        assertEquals(CountryCapabilityCatalog.defaultProfile().iso2, display.countryIso2)
    }

    @Test
    fun `language variant falls back to base language when exact match is absent`() {
        val display = resolveLanguageDisplaySpec("de-AT")

        assertEquals("de", display.code)
        assertEquals("DE", display.countryIso2)
    }
}

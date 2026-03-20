package net.metalbrain.paysmart.core.features.capabilities.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountLimitJsonParserTest {

    @Test
    fun parsePreservesTabsAndSectionTemplatesIndependently() {
        val parsed = AccountLimitJsonParser.parse(
            """
            {
              "schemaVersion": "test-v1",
              "accountLimitSets": {
                "gb": {
                  "tabs": [
                    { "key": "send", "title": "Limit on send" },
                    { "key": "receive", "title": "Limit on receive" }
                  ],
                  "sections": [
                    { "key": "single", "title": "Send Limit (Single transaction)", "type": "single" },
                    { "key": "daily", "title": "Daily limit of {amount}", "type": "period" }
                  ]
                }
              }
            }
            """.trimIndent()
        )

        val entity = parsed.entities.single()
        val tabs = parseTabsJson(entity.tabsJson)
        val sections = parseSectionTemplatesJson(entity.sectionsJson)

        assertEquals("test-v1", parsed.catalogVersion)
        assertEquals("GB", entity.iso2)
        assertEquals(listOf(AccountLimitKey.SEND, AccountLimitKey.RECEIVE), tabs.map { it.key })
        assertEquals(listOf("single", "daily"), sections.map { it.key })
        assertEquals(AccountLimitSectionType.SINGLE, sections.first().type)
        assertEquals("Daily limit of {amount}", sections.last().titleTemplate)
    }
}

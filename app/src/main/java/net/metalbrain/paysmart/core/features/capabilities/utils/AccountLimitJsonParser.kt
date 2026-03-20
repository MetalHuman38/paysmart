package net.metalbrain.paysmart.core.features.capabilities.utils

import net.metalbrain.paysmart.room.entity.CountryAccountLimitEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

internal object AccountLimitJsonParser {

    fun parse(raw: String): ParsedAccountLimitCatalog {
        val root = JSONObject(raw)

        val version = root.optString("schemaVersion").ifBlank { "v1" }

        val sets = root.optJSONObject("accountLimitSets") ?: JSONObject()

        val entities = mutableListOf<CountryAccountLimitEntity>()

        sets.keys().forEach { key ->

            val obj = sets.optJSONObject(key) ?: return@forEach

            val iso2 = key.uppercase(Locale.US)

            val tabs = obj.optJSONArray("tabs") ?: JSONArray()
            val sections = obj.optJSONArray("sections") ?: JSONArray()

            entities += CountryAccountLimitEntity(
                iso2 = iso2,
                tabsJson = tabs.toString(),
                sectionsJson = sections.toString(),
                catalogVersion = version
            )
        }

        return ParsedAccountLimitCatalog(
            catalogVersion = version,
            entities = entities
        )
    }
}

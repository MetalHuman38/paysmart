package net.metalbrain.paysmart.core.features.capabilities.utils

import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionTemplate
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionType
import org.json.JSONArray

fun parseSectionTemplatesJson(raw: String): List<AccountLimitSectionTemplate> {
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: JSONArray()
    val result = mutableListOf<AccountLimitSectionTemplate>()

    for (index in 0 until array.length()) {
        val obj = array.optJSONObject(index) ?: continue

        val key = obj.optString("key").trim()
        val titleTemplate = obj.optString("title").trim()
        val type = AccountLimitSectionType.fromRawOrNull(obj.optString("type"))

        if (key.isBlank() || titleTemplate.isBlank() || type == null) continue

        result += AccountLimitSectionTemplate(
            key = key,
            titleTemplate = titleTemplate,
            type = type
        )
    }

    return result.ifEmpty {
        AccountLimitCatalog.defaultSectionTemplates()
    }
}

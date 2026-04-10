package net.metalbrain.paysmart.core.features.capabilities.utils

import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitTabSpec
import org.json.JSONArray

fun parseTabsJson(raw: String): List<AccountLimitTabSpec> {
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: JSONArray()

    val result = mutableListOf<AccountLimitTabSpec>()

    for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i) ?: continue

        val key = obj.optString("key")
        val title = obj.optString("title")

        if (key.isBlank() || title.isBlank()) continue

        val accountLimitKey = AccountLimitKey.fromRawOrNull(key) ?: continue

        result += AccountLimitTabSpec(
            key = accountLimitKey,
            title = title
        )
    }

    return result.ifEmpty {
        AccountLimitCatalog.defaultTabs()
    }
}

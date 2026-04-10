package net.metalbrain.paysmart.core.features.capabilities.catalog

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.metalbrain.paysmart.data.wallet.R
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountLimitValueCatalog @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val profiles: Map<String, AccountLimitValueProfile> by lazy {
        AccountLimitValueJsonParser.parse(
            context.resources.openRawResource(R.raw.account_limit_values)
                .bufferedReader()
                .use { it.readText() }
        )
    }

    fun valuesForIso2(rawIso2: String?): AccountLimitValueProfile {
        val iso2 = rawIso2
            ?.trim()
            ?.uppercase(Locale.US)
            .orEmpty()

        return profiles[iso2]
            ?: profiles[DEFAULT_KEY]
            ?: DEFAULT_PROFILE
    }

    private companion object {
        const val DEFAULT_KEY = "DEFAULT"
        val DEFAULT_PROFILE = AccountLimitValueProfile(
            iso2 = DEFAULT_KEY,
            send = mapOf(
                "single" to 10_000.0,
                "daily" to 10_000.0,
                "weekly" to 20_000.0,
                "monthly" to 40_000.0
            ),
            receive = mapOf(
                "single" to 10_000.0,
                "daily" to 10_000.0,
                "weekly" to 20_000.0,
                "monthly" to 40_000.0
            )
        )
    }
}

internal object AccountLimitValueJsonParser {

    fun parse(raw: String): Map<String, AccountLimitValueProfile> {
        val root = JSONObject(raw)
        val sets = root.optJSONObject("limitValueSets") ?: JSONObject()
        val result = linkedMapOf<String, AccountLimitValueProfile>()

        sets.keys().forEach { key ->
            val entry = sets.optJSONObject(key) ?: return@forEach
            val normalizedKey = key.trim().uppercase(Locale.US)

            result[normalizedKey] = AccountLimitValueProfile(
                iso2 = normalizedKey,
                send = parseLimits(entry.optJSONObject("send")),
                receive = parseLimits(entry.optJSONObject("receive"))
            )
        }

        return result
    }

    private fun parseLimits(raw: JSONObject?): Map<String, Double> {
        if (raw == null) return emptyMap()

        val result = linkedMapOf<String, Double>()
        raw.keys().forEach { key ->
            val value = raw.optDouble(key)
            if (!value.isNaN()) {
                result[key.trim()] = value
            }
        }
        return result
    }
}

package net.metalbrain.paysmart.core.features.capabilities.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.capabilities.catalog.AddMoneyMarketPolicy
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.ui.R
import net.metalbrain.paysmart.room.dao.CountryCapabilityDao
import net.metalbrain.paysmart.room.entity.CountryCapabilityEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryCapabilityRepository @Inject constructor(
    private val dao: CountryCapabilityDao,
    @param:ApplicationContext private val context: Context
) {
    private val seedMutex = Mutex()

    @Volatile
    private var seeded: Boolean = false

    @Volatile
    private var countryNameAliases: Map<String, String> = emptyMap()

    private val staticAliases: Map<String, String> = buildStaticAliases()

    fun observeProfile(countryHint: String?): Flow<CountryCapabilityProfile> = flow {
        ensureSeeded()
        val iso2 = normalizeCountryHint(countryHint)
        emitAll(
            combine(
                dao.observeByIso2(iso2),
                dao.observeByIso2(CountryCapabilityCatalog.DEFAULT_ISO2)
            ) { selected, fallback ->
                selected?.toDomain() ?: fallback?.toDomain() ?: CountryCapabilityCatalog.defaultProfile()
            }
        )
    }

    fun observeAllProfiles(): Flow<List<CountryCapabilityProfile>> = flow {
        ensureSeeded()
        emitAll(
            dao.observeAll().map { entities ->
                entities.map { it.toDomain() }
            }
        )
    }

    suspend fun ensureSeeded() {
        if (seeded) return

        seedMutex.withLock {
            if (seeded) return

            val raw = context.resources
                .openRawResource(R.raw.country_capabilities_catalog)
                .bufferedReader()
                .use { it.readText() }

            val parsed = CountryCapabilityJsonParser.parse(raw)
            countryNameAliases = parsed.countryAliases

            val currentVersion = dao.getCatalogVersion()
            val currentCount = dao.count()
            val shouldRefresh = currentCount == 0 || currentVersion != parsed.catalogVersion

            if (shouldRefresh) {
                dao.clearAll()
                dao.upsertAll(parsed.entities)
            }

            seeded = true
        }
    }

    private fun normalizeCountryHint(countryHint: String?): String {
        val cleaned = countryHint
            ?.trim()
            ?.uppercase(Locale.US)
            .orEmpty()
        if (cleaned.isBlank()) return CountryCapabilityCatalog.DEFAULT_ISO2

        if (cleaned.length == 2 && cleaned.all { it in 'A'..'Z' }) {
            return cleaned
        }

        val alias = countryNameAliases[cleaned]
            ?: staticAliases[cleaned]
        return alias ?: CountryCapabilityCatalog.DEFAULT_ISO2
    }

    private fun buildStaticAliases(): Map<String, String> {
        val aliases = mutableMapOf(
            "UK" to "GB",
            "UNITED KINGDOM" to "GB",
            "GREAT BRITAIN" to "GB",
            "ENGLAND" to "GB",
            "SCOTLAND" to "GB",
            "WALES" to "GB",
            "USA" to "US",
            "UNITED STATES" to "US",
            "UNITED STATES OF AMERICA" to "US",
            "AMERICA" to "US",
            "CANADA" to "CA",
            "AUSTRALIA" to "AU",
            "NEW ZEALAND" to "NZ",
            "SWITZERLAND" to "CH",
            "SWISS" to "CH",
            "SWISSLAND" to "CH",
        )

        Locale.getISOCountries().forEach { iso2 ->
            val locale = Locale.Builder().setRegion(iso2).build()
            val iso2Upper = iso2.uppercase(Locale.US)
            val displayName = locale.displayCountry.uppercase(Locale.US)
            aliases.putIfAbsent(displayName, iso2Upper)
            val iso3 = runCatching { locale.isO3Country.uppercase(Locale.US) }.getOrNull()
            if (!iso3.isNullOrBlank()) {
                aliases.putIfAbsent(iso3, iso2Upper)
            }
        }
        return aliases
    }
}

private fun CountryCapabilityEntity.toDomain(): CountryCapabilityProfile {
    return CountryCapabilityProfile(
        iso2 = iso2,
        countryName = countryName,
        flagEmoji = flagEmoji,
        currencyCode = currencyCode,
        addMoney = AddMoneyMarketPolicy(
            providers = parseAddMoneyProviders(addMoneyProvidersJson),
            methods = parsePaymentMethods(addMoneyMethodsJson)
        ),
        capabilities = parseCapabilities(capabilitiesJson)
    )
}

private fun parseAddMoneyProviders(raw: String): List<AddMoneyProvider> {
    val result = mutableListOf<AddMoneyProvider>()
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: JSONArray()
    for (index in 0 until array.length()) {
        val provider = AddMoneyProvider.fromRawOrNull(array.optString(index)) ?: continue
        if (provider !in result) {
            result += provider
        }
    }
    return result
}

private fun parsePaymentMethods(raw: String): List<FxPaymentMethod> {
    val result = mutableListOf<FxPaymentMethod>()
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: JSONArray()
    for (index in 0 until array.length()) {
        val method = when (array.optString(index).trim().lowercase(Locale.US)) {
            "wire" -> FxPaymentMethod.WIRE
            "debitcard", "debit_card" -> FxPaymentMethod.DEBIT_CARD
            "creditcard", "credit_card" -> FxPaymentMethod.CREDIT_CARD
            "accounttransfer", "account_transfer" -> FxPaymentMethod.ACCOUNT_TRANSFER
            else -> null
        }
        if (method != null) {
            result += method
        }
    }
    return result.ifEmpty {
        listOf(FxPaymentMethod.DEBIT_CARD, FxPaymentMethod.CREDIT_CARD)
    }
}

private fun parseCapabilities(raw: String): List<CapabilityItem> {
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: JSONArray()
    val result = mutableListOf<CapabilityItem>()
    for (index in 0 until array.length()) {
        val item = array.optJSONObject(index) ?: continue
        val title = item.optString("title").trim()
        val subtitle = item.optString("subtitle").trim()
        if (title.isBlank() || subtitle.isBlank()) continue

        result += CapabilityItem(
            key = CapabilityKey.fromRaw(item.optString("key")),
            title = title,
            subtitle = subtitle,
            footnote = item.optString("footnote").trim().ifBlank { null }
        )
    }
    return result.ifEmpty { CountryCapabilityCatalog.defaultProfile().capabilities }
}

internal data class ParsedCountryCapabilityCatalog(
    val catalogVersion: String,
    val entities: List<CountryCapabilityEntity>,
    val countryAliases: Map<String, String>
)

internal object CountryCapabilityJsonParser {
    fun parse(raw: String): ParsedCountryCapabilityCatalog {
        val root = JSONObject(raw)
        val catalogVersion = root.optString("schemaVersion").ifBlank { "v1" }
        val capabilitySets = parseCapabilitySets(root.optJSONObject("capabilitySets"))
        val countries = root.optJSONArray("countries") ?: JSONArray()

        val entities = mutableListOf<CountryCapabilityEntity>()
        val aliases = mutableMapOf<String, String>()

        for (index in 0 until countries.length()) {
            val country = countries.optJSONObject(index) ?: continue
            val iso2 = country.optString("iso2").trim().uppercase(Locale.US)
            if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) continue

            val name = country.optString("name").trim().ifBlank { iso2 }
            val iso3 = country.optString("iso3").trim().uppercase(Locale.US)
            val flag = country.optString("flag").trim().ifBlank { isoToFlagEmoji(iso2) }
            val currencyCode = country.optString("currencyCode").trim()
                .uppercase(Locale.US)
                .ifBlank { "GBP" }
            val providers = country.optJSONArray("addMoneyProviders")
                ?: JSONArray()

            val methods = country.optJSONArray("addMoneyMethods")
                ?: JSONArray().put("debitCard").put("creditCard")

            val capabilities = country.optJSONArray("capabilities")
                ?: capabilitySets[country.optString("capabilitySet").trim()]
                ?: capabilitySets["default"]
                ?: JSONArray()

            entities += CountryCapabilityEntity(
                iso2 = iso2,
                countryName = name,
                flagEmoji = flag,
                currencyCode = currencyCode,
                addMoneyProvidersJson = providers.toString(),
                addMoneyMethodsJson = methods.toString(),
                capabilitiesJson = capabilities.toString(),
                catalogVersion = catalogVersion
            )

            aliases[name.uppercase(Locale.US)] = iso2
            if (iso3.isNotBlank()) {
                aliases[iso3] = iso2
            }
        }

        return ParsedCountryCapabilityCatalog(
            catalogVersion = catalogVersion,
            entities = entities,
            countryAliases = aliases
        )
    }

    private fun parseCapabilitySets(rawSets: JSONObject?): Map<String, JSONArray> {
        if (rawSets == null) return emptyMap()
        val result = mutableMapOf<String, JSONArray>()
        rawSets.keys().forEach { key ->
            val values = rawSets.optJSONArray(key) ?: JSONArray()
            result[key.trim()] = values
        }
        return result
    }

    private fun isoToFlagEmoji(iso2: String): String {
        val normalized = iso2.trim().uppercase(Locale.US)
        if (normalized.length != 2 || normalized.any { it !in 'A'..'Z' }) return "\uD83C\uDF0D"
        return normalized.map { char ->
            String(Character.toChars(0x1F1E6 + (char.code - 'A'.code)))
        }.joinToString(separator = "")
    }
}

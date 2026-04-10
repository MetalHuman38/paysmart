package net.metalbrain.paysmart.core.features.capabilities.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitMarketProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountTypeAndLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.utils.AccountLimitJsonParser
import net.metalbrain.paysmart.core.features.capabilities.utils.parseSectionTemplatesJson
import net.metalbrain.paysmart.core.features.capabilities.utils.parseTabsJson
import net.metalbrain.paysmart.data.wallet.R
import net.metalbrain.paysmart.room.dao.CountryAccountLimitDao
import net.metalbrain.paysmart.room.entity.CountryAccountLimitEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryAccountLimitRepository @Inject constructor(
    private val dao: CountryAccountLimitDao,
    private val accountTypeAndLimitCatalog: AccountTypeAndLimitCatalog,
    @param:ApplicationContext private val context: Context
) {

    private val seedMutex = Mutex()

    @Volatile
    private var seeded: Boolean = false

    fun observeProfile(countryHint: String?): Flow<AccountLimitProfile> = flow {
        ensureSeeded()

        val iso2 = normalizeCountryHint(countryHint)

        emitAll(
            combine(
                dao.observeByIso2(iso2),
                dao.observeByIso2(AccountLimitCatalog.DEFAULT_ISO2)
            ) { selected, fallback ->

                selected?.toDomain()
                    ?: fallback?.toDomain()
                    ?: AccountLimitCatalog.defaultProfile()
            }
        )
    }

    suspend fun ensureSeeded() {
        if (seeded) return

        seedMutex.withLock {
            if (seeded) return

            val raw = context.resources
                .openRawResource(R.raw.account_limits_properties)
                .bufferedReader()
                .use { it.readText() }

            val parsed = AccountLimitJsonParser.parse(raw)

            val currentVersion = dao.getCatalogVersion()
            val currentCount = dao.count()

            val shouldRefresh =
                currentCount == 0 || currentVersion != parsed.catalogVersion

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

        if (cleaned.length == 2) return cleaned

        return AccountLimitCatalog.DEFAULT_ISO2
    }

    private fun CountryAccountLimitEntity.toDomain(): AccountLimitProfile {
        val marketProfile = accountTypeAndLimitCatalog.profileForIso2(iso2)
            ?: accountTypeAndLimitCatalog.profileForIso2(AccountLimitCatalog.DEFAULT_ISO2)
            ?: AccountLimitCatalog.defaultProfile().let { defaultProfile ->
                AccountLimitMarketProfile(
                    iso2 = defaultProfile.iso2,
                    countryName = defaultProfile.countryName,
                    flagEmoji = defaultProfile.flagEmoji,
                    currencyCode = defaultProfile.currencyCode,
                    currencyName = defaultProfile.currencyCode,
                    currencySymbol = defaultProfile.currencyCode,
                    supportsIban = false,
                    supportsLocalAccount = false
                )
            }

        return AccountLimitProfile(
            iso2 = iso2,
            countryName = marketProfile.countryName,
            flagEmoji = marketProfile.flagEmoji,
            currencyCode = marketProfile.currencyCode,
            tabs = parseTabsJson(tabsJson),
            sectionTemplates = parseSectionTemplatesJson(sectionsJson)
        )
    }
}

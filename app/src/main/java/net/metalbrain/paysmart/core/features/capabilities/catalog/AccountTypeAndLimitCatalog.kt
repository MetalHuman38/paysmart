package net.metalbrain.paysmart.core.features.capabilities.catalog

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.metalbrain.paysmart.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class AccountLimitMarketProfile(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val currencyName: String,
    val currencySymbol: String,
    val supportsIban: Boolean,
    val supportsLocalAccount: Boolean
)

@Singleton
class AccountTypeAndLimitCatalog @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val entries: List<AccountLimitMarketProfile> by lazy {
        AccountTypeAndLimitJsonParser.parse(
            context.resources.openRawResource(R.raw.account_type_and_limit)
                .bufferedReader()
                .use { it.readText() }
        )
    }

    fun entries(): List<AccountLimitMarketProfile> = entries

    fun profileForIso2(rawIso2: String?): AccountLimitMarketProfile? {
        val iso2 = rawIso2
            ?.trim()
            ?.uppercase(Locale.US)
            .orEmpty()
        if (iso2.length != 2) return null
        return entries.firstOrNull { it.iso2 == iso2 }
    }

    fun resolveMarketForCurrency(
        rawCurrencyCode: String,
        preferredIso2: String?
    ): AccountLimitMarketProfile? {
        return resolveMarketForCurrency(
            entries = entries,
            rawCurrencyCode = rawCurrencyCode,
            preferredIso2 = preferredIso2
        )
    }

    fun currencyDisplayName(rawCurrencyCode: String): String {
        return CountrySelectionCatalog.currencyByCode(context, rawCurrencyCode)
            ?.displayName
            ?.ifBlank { null }
            ?: resolveMarketForCurrency(rawCurrencyCode, preferredIso2 = null)
                ?.currencyName
            ?: rawCurrencyCode.trim().uppercase(Locale.US)
    }

    fun accountDescriptor(profile: AccountLimitMarketProfile?): String {
        if (profile == null) {
            return context.getString(R.string.account_limits_account_descriptor_bank_transfer)
        }

        return resolveDescriptor(
            profile = profile,
            accountNumberLabel = context.getString(R.string.funding_account_details_account_number),
            ibanLabel = context.getString(R.string.send_money_field_iban),
            bankTransferLabel = context.getString(R.string.account_limits_account_descriptor_bank_transfer)
        )
    }

    companion object {
        internal fun resolveMarketForCurrency(
            entries: List<AccountLimitMarketProfile>,
            rawCurrencyCode: String,
            preferredIso2: String?
        ): AccountLimitMarketProfile? {
            val currencyCode = rawCurrencyCode.trim().uppercase(Locale.US)
            if (currencyCode.length != 3) return null

            val matches = entries.filter { it.currencyCode == currencyCode }
            if (matches.isEmpty()) return null

            val normalizedPreferredIso2 = preferredIso2
                ?.trim()
                ?.uppercase(Locale.US)
                .orEmpty()

            if (normalizedPreferredIso2.length == 2) {
                matches.firstOrNull { it.iso2 == normalizedPreferredIso2 }?.let { return it }
            }

            return matches.first()
        }

        internal fun resolveDescriptor(
            profile: AccountLimitMarketProfile,
            accountNumberLabel: String,
            ibanLabel: String,
            bankTransferLabel: String
        ): String {
            return when {
                profile.supportsLocalAccount && profile.supportsIban ->
                    "$accountNumberLabel, $ibanLabel"

                profile.supportsLocalAccount ->
                    accountNumberLabel

                profile.supportsIban ->
                    ibanLabel

                else ->
                    bankTransferLabel
            }
        }
    }
}

internal object AccountTypeAndLimitJsonParser {

    fun parse(raw: String): List<AccountLimitMarketProfile> {
        val array = JSONArray(raw)
        val result = mutableListOf<AccountLimitMarketProfile>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val iso2 = item.optString("iso2").trim().uppercase(Locale.US)
            if (iso2.length != 2) continue

            val currency = item.optJSONObject("currency") ?: JSONObject()
            val payoutRules = item.optJSONObject("payoutRules")
                ?.optJSONObject("bankTransfer")

            result += AccountLimitMarketProfile(
                iso2 = iso2,
                countryName = item.optString("name").trim().ifBlank { iso2 },
                flagEmoji = item.optString("flag").trim().ifBlank { "🌍" },
                currencyCode = currency.optString("code").trim().uppercase(Locale.US),
                currencyName = currency.optString("name").trim().ifBlank { iso2 },
                currencySymbol = currency.optString("symbol").trim()
                    .ifBlank { currency.optString("code").trim().uppercase(Locale.US) },
                supportsIban = payoutRules?.optBoolean("supportsIban") == true || payoutRules?.has("iban") == true,
                supportsLocalAccount = payoutRules?.optJSONObject("local") != null
            )
        }

        return result
    }
}

package net.metalbrain.paysmart.core.features.capabilities.catalog

data class AccountLimitProfile(
    val iso2: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val tabs: List<AccountLimitTabSpec> = emptyList(),
    val sectionTemplates: List<AccountLimitSectionTemplate> = emptyList()
)

data class AccountLimitTabSpec(
    val key: AccountLimitKey,
    val title: String
)

enum class AccountLimitSectionType {
    SINGLE,
    PERIOD;

    companion object {
        fun fromRawOrNull(raw: String?): AccountLimitSectionType? {
            return when (raw?.trim()?.lowercase()) {
                "single" -> SINGLE
                "period" -> PERIOD
                else -> null
            }
        }
    }
}

data class AccountLimitSectionTemplate(
    val key: String,
    val titleTemplate: String,
    val type: AccountLimitSectionType
)

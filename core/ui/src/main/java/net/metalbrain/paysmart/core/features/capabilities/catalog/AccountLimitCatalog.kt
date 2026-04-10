package net.metalbrain.paysmart.core.features.capabilities.catalog

object AccountLimitCatalog {

    const val DEFAULT_ISO2: String = "GB"

    fun defaultProfile(): AccountLimitProfile {
        return AccountLimitProfile(
            iso2 = DEFAULT_ISO2,
            countryName = "United Kingdom",
            flagEmoji = "\uD83C\uDDEC\uD83C\uDDE7",
            currencyCode = "GBP",
            tabs = defaultTabs(),
            sectionTemplates = defaultSectionTemplates()
        )
    }

    fun defaultTabs(): List<AccountLimitTabSpec> {
        return listOf(
            AccountLimitTabSpec(
                key = AccountLimitKey.SEND,
                title = "Limit on send"
            ),
            AccountLimitTabSpec(
                key = AccountLimitKey.RECEIVE,
                title = "Limit on receive"
            )
        )
    }

    fun defaultSectionTemplates(): List<AccountLimitSectionTemplate> {
        return listOf(
            AccountLimitSectionTemplate(
                key = "single",
                titleTemplate = "Send Limit (Single transaction)",
                type = AccountLimitSectionType.SINGLE
            ),
            AccountLimitSectionTemplate(
                key = "daily",
                titleTemplate = "Daily limit of {amount}",
                type = AccountLimitSectionType.PERIOD
            ),
            AccountLimitSectionTemplate(
                key = "weekly",
                titleTemplate = "Weekly limit of {amount}",
                type = AccountLimitSectionType.PERIOD
            ),
            AccountLimitSectionTemplate(
                key = "monthly",
                titleTemplate = "Monthly limit of {amount}",
                type = AccountLimitSectionType.PERIOD
            )
        )
    }
}

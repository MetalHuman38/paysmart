package net.metalbrain.paysmart.core.features.capabilities.catalog

data class AccountLimitValueProfile(
    val iso2: String,
    val send: Map<String, Double>,
    val receive: Map<String, Double>
) {
    fun limitFor(
        tab: AccountLimitKey,
        sectionKey: String
    ): Double? {
        return when (tab) {
            AccountLimitKey.SEND -> send[sectionKey]
            AccountLimitKey.RECEIVE -> receive[sectionKey]
            else -> null
        }
    }
}

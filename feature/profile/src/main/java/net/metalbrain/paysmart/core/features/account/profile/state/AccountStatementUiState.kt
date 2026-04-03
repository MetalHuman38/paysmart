package net.metalbrain.paysmart.core.features.account.profile.state

import java.time.LocalDate

enum class AccountStatementFormat {
    PDF
}

data class AccountStatementUiState(
    val selectedCurrencyCode: String = "GBP",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val format: AccountStatementFormat = AccountStatementFormat.PDF
) {
    val canRequestStatement: Boolean
        get() = startDate != null && endDate != null && !startDate.isAfter(endDate)
}

package net.metalbrain.paysmart.core.features.sendmoney.domain

data class RecentSendRecipient(
    val recipientKey: String,
    val selectedMethod: RecipientMethod,
    val sourceCurrency: String = "GBP",
    val targetCurrency: String = "EUR",
    val displayName: String,
    val subtitle: String,
    val voltpayLookup: VoltpayLookupRecipientForm = VoltpayLookupRecipientForm(),
    val bankDetails: BankRecipientForm = BankRecipientForm(),
    val emailRequest: EmailRequestRecipientForm = EmailRequestRecipientForm(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    fun toPrefilledDraft(): SendMoneyRecipientDraft {
        return SendMoneyRecipientDraft(
            selectedMethod = selectedMethod,
            step = RecipientFlowStep.METHOD_PICKER,
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            sourceAmountInput = "",
            quoteSnapshot = null,
            quoteDataSource = null,
            voltpayLookup = voltpayLookup,
            bankDetails = bankDetails,
            emailRequest = emailRequest,
            documentUpload = DocumentRecipientForm(),
            updatedAtMs = updatedAtMs
        )
    }
}

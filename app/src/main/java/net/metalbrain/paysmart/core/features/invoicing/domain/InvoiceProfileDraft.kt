package net.metalbrain.paysmart.core.features.invoicing.domain

/**
 * Represents a mutable draft of an invoice profile, containing personal details,
 * tax information, and payment preferences required to generate invoices.
 *
 * @property fullName The full legal name of the individual or entity.
 * @property address The registered or residential address for the profile.
 * @property badgeNumber The professional or identification badge number.
 * @property badgeExpiryDate The expiration date of the associated badge.
 * @property utrNumber The Unique Taxpayer Reference (UTR) number.
 * @property email The contact email address.
 * @property contactPhone The contact phone number.
 * @property paymentMethod The preferred method of receiving payments (e.g., Bank Transfer).
 * @property accountNumber The bank account number for receiving payments.
 * @property sortCode The bank sort code for receiving payments.
 * @property paymentInstructions Additional instructions for the payer regarding the payment.
 * @property defaultHourlyRateInput The default hourly rate to be used in new invoices.
 * @property declaration The legal or professional declaration statement included in invoices.
 * @property updatedAtMs The timestamp in milliseconds when this draft was last modified.
 */
data class InvoiceProfileDraft(
    val fullName: String = "",
    val address: String = "",
    val badgeNumber: String = "",
    val badgeExpiryDate: String = "",
    val utrNumber: String = "",
    val email: String = "",
    val contactPhone: String = "",
    val paymentMethod: InvoicePaymentMethod = InvoicePaymentMethod.BANK_TRANSFER,
    val accountNumber: String = "",
    val sortCode: String = "",
    val paymentInstructions: String = "",
    val defaultHourlyRateInput: String = "",
    val declaration: String = DEFAULT_INVOICE_DECLARATION,
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() &&
            address.isNotBlank() &&
            badgeNumber.isNotBlank() &&
            badgeExpiryDate.isNotBlank() &&
            utrNumber.isNotBlank() &&
            email.isNotBlank()

    fun normalized(nowMs: Long = System.currentTimeMillis()): InvoiceProfileDraft {
        return copy(
            fullName = fullName.trim(),
            address = address.trim(),
            badgeNumber = badgeNumber.trim(),
            badgeExpiryDate = badgeExpiryDate.trim(),
            utrNumber = utrNumber.trim(),
            email = email.trim(),
            contactPhone = contactPhone.trim(),
            accountNumber = accountNumber.trim(),
            sortCode = sortCode.trim(),
            paymentInstructions = paymentInstructions.trim(),
            defaultHourlyRateInput = defaultHourlyRateInput.trim(),
            declaration = declaration.trim().ifBlank { DEFAULT_INVOICE_DECLARATION },
            updatedAtMs = nowMs
        )
    }
}

enum class InvoicePaymentMethod(val storageKey: String) {
    BANK_TRANSFER("bank_transfer"),
    OTHER("other");

    companion object {
        fun fromStorage(raw: String?): InvoicePaymentMethod {
            return entries.firstOrNull { it.storageKey == raw } ?: BANK_TRANSFER
        }
    }
}

const val DEFAULT_INVOICE_DECLARATION: String =
    "I confirm that I am self-employed and responsible for my own tax and national insurance contributions."

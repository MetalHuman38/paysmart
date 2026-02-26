package net.metalbrain.paysmart.core.features.sendmoney.domain

import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import java.util.Locale

enum class RecipientMethod(val storageKey: String) {
    VOLTPAY_LOOKUP("voltpay_lookup"),
    BANK_DETAILS("bank_details"),
    DOCUMENT_UPLOAD("document_upload"),
    EMAIL_REQUEST("email_request");

    companion object {
        fun fromStorage(raw: String?): RecipientMethod {
            return entries.firstOrNull { it.storageKey == raw } ?: VOLTPAY_LOOKUP
        }
    }
}

enum class RecipientFlowStep(val storageKey: String) {
    METHOD_PICKER("method_picker"),
    DETAILS("details"),
    REVIEW("review"),
    DONE("done");

    companion object {
        fun fromStorage(raw: String?): RecipientFlowStep {
            return entries.firstOrNull { it.storageKey == raw } ?: METHOD_PICKER
        }
    }
}

data class VoltpayLookupRecipientForm(
    val voltTag: String = "",
    val email: String = "",
    val mobile: String = "",
    val note: String = ""
) {
    val isValid: Boolean
        get() = voltTag.isNotBlank() || email.isNotBlank() || mobile.isNotBlank()
}

data class BankRecipientForm(
    val fullName: String = "",
    val iban: String = "",
    val bic: String = "",
    val swift: String = "",
    val bankName: String = "",
    val bankAddress: String = "",
    val bankCity: String = "",
    val bankCountry: String = "",
    val bankPostalCode: String = ""
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() && iban.isNotBlank()
}

data class DocumentRecipientForm(
    val fileRef: String = "",
    val docType: String = "",
    val note: String = ""
) {
    val isValid: Boolean
        get() = fileRef.isNotBlank()
}

data class EmailRequestRecipientForm(
    val email: String = "",
    val fullName: String = "",
    val note: String = ""
) {
    val isValid: Boolean
        get() = email.isNotBlank()
}

data class SendMoneyRecipientDraft(
    val selectedMethod: RecipientMethod = RecipientMethod.VOLTPAY_LOOKUP,
    val step: RecipientFlowStep = RecipientFlowStep.METHOD_PICKER,
    val sourceCurrency: String = "GBP",
    val targetCurrency: String = "EUR",
    val sourceAmountInput: String = "",
    val quoteMethod: FxPaymentMethod = FxPaymentMethod.WIRE,
    val quoteSnapshot: FxQuote? = null,
    val quoteDataSource: FxQuoteDataSource? = null,
    val voltpayLookup: VoltpayLookupRecipientForm = VoltpayLookupRecipientForm(),
    val bankDetails: BankRecipientForm = BankRecipientForm(),
    val documentUpload: DocumentRecipientForm = DocumentRecipientForm(),
    val emailRequest: EmailRequestRecipientForm = EmailRequestRecipientForm(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    fun withUpdatedTimestamp(nowMs: Long = System.currentTimeMillis()): SendMoneyRecipientDraft {
        return copy(updatedAtMs = nowMs)
    }

    fun normalized(): SendMoneyRecipientDraft {
        return copy(
            sourceCurrency = sourceCurrency.trim().uppercase(Locale.US),
            targetCurrency = targetCurrency.trim().uppercase(Locale.US),
            sourceAmountInput = sourceAmountInput.trim(),
            voltpayLookup = voltpayLookup.copy(
                voltTag = voltpayLookup.voltTag.trim(),
                email = voltpayLookup.email.trim(),
                mobile = voltpayLookup.mobile.trim(),
                note = voltpayLookup.note.trim()
            ),
            bankDetails = bankDetails.copy(
                fullName = bankDetails.fullName.trim(),
                iban = bankDetails.iban.trim(),
                bic = bankDetails.bic.trim(),
                swift = bankDetails.swift.trim(),
                bankName = bankDetails.bankName.trim(),
                bankAddress = bankDetails.bankAddress.trim(),
                bankCity = bankDetails.bankCity.trim(),
                bankCountry = bankDetails.bankCountry.trim(),
                bankPostalCode = bankDetails.bankPostalCode.trim()
            ),
            documentUpload = documentUpload.copy(
                fileRef = documentUpload.fileRef.trim(),
                docType = documentUpload.docType.trim(),
                note = documentUpload.note.trim()
            ),
            emailRequest = emailRequest.copy(
                email = emailRequest.email.trim(),
                fullName = emailRequest.fullName.trim(),
                note = emailRequest.note.trim()
            )
        )
    }

    fun isSelectedMethodValid(): Boolean {
        val current = normalized()
        return when (current.selectedMethod) {
            RecipientMethod.VOLTPAY_LOOKUP -> current.voltpayLookup.isValid
            RecipientMethod.BANK_DETAILS -> current.bankDetails.isValid
            RecipientMethod.DOCUMENT_UPLOAD -> current.documentUpload.isValid
            RecipientMethod.EMAIL_REQUEST -> current.emailRequest.isValid
        }
    }
}

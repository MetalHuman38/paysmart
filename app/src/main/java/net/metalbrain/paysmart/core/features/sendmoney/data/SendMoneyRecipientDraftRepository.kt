package net.metalbrain.paysmart.core.features.sendmoney.data

import com.google.gson.Gson
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.room.dao.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.entity.SendMoneyRecipientDraftEntity
import net.metalbrain.paysmart.core.features.fx.data.FxFeeLine
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import net.metalbrain.paysmart.core.features.sendmoney.domain.BankRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.DocumentRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.EmailRequestRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientFlowStep
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm

@Singleton
class SendMoneyRecipientDraftRepository @Inject constructor(
    private val dao: SendMoneyRecipientDraftDao
) {
    fun observeByUserId(userId: String): Flow<SendMoneyRecipientDraft?> {
        return dao.observeByUserId(userId).map { entity -> entity?.toDomain() }
    }

    suspend fun getByUserId(userId: String): SendMoneyRecipientDraft? {
        return dao.getByUserId(userId)?.toDomain()
    }

    suspend fun upsert(userId: String, draft: SendMoneyRecipientDraft) {
        val normalized = draft.normalized().withUpdatedTimestamp()
        dao.upsert(normalized.toEntity(userId))
    }

    suspend fun clear(userId: String) {
        dao.deleteByUserId(userId)
    }
}

private val gson = Gson()

private data class FxQuotePayload(
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val rate: Double,
    val recipientAmount: Double,
    val fees: List<FxQuoteFeePayload>,
    val guaranteeSeconds: Int,
    val arrivalSeconds: Int,
    val rateSource: String,
    val updatedAtMs: Long
)

private data class FxQuoteFeePayload(
    val label: String,
    val amount: Double,
    val code: String?
)

private fun SendMoneyRecipientDraft.toEntity(userId: String): SendMoneyRecipientDraftEntity {
    return SendMoneyRecipientDraftEntity(
        userId = userId,
        selectedMethod = selectedMethod.storageKey,
        step = step.storageKey,
        sourceCurrency = sourceCurrency,
        targetCurrency = targetCurrency,
        sourceAmountInput = sourceAmountInput,
        quoteMethod = quoteMethod.apiCode,
        quotePayloadJson = quoteSnapshot?.toJsonPayload(),
        quoteDataSource = quoteDataSource?.name,
        voltTag = voltpayLookup.voltTag,
        lookupEmail = voltpayLookup.email,
        lookupMobile = voltpayLookup.mobile,
        lookupNote = voltpayLookup.note,
        bankFullName = bankDetails.fullName,
        bankIban = bankDetails.iban,
        bankBic = bankDetails.bic,
        bankSwift = bankDetails.swift,
        bankName = bankDetails.bankName,
        bankAddress = bankDetails.bankAddress,
        bankCity = bankDetails.bankCity,
        bankCountry = bankDetails.bankCountry,
        bankPostalCode = bankDetails.bankPostalCode,
        documentFileRef = documentUpload.fileRef,
        documentType = documentUpload.docType,
        documentNote = documentUpload.note,
        requestEmail = emailRequest.email,
        requestFullName = emailRequest.fullName,
        requestNote = emailRequest.note,
        updatedAtMs = updatedAtMs
    )
}

private fun SendMoneyRecipientDraftEntity.toDomain(): SendMoneyRecipientDraft {
    return SendMoneyRecipientDraft(
        selectedMethod = RecipientMethod.fromStorage(selectedMethod),
        step = RecipientFlowStep.fromStorage(step),
        sourceCurrency = sourceCurrency,
        targetCurrency = targetCurrency,
        sourceAmountInput = sourceAmountInput,
        quoteMethod = FxPaymentMethod.fromApiCode(quoteMethod),
        quoteSnapshot = quotePayloadJson.toQuoteOrNull(),
        quoteDataSource = FxQuoteDataSource.entries.firstOrNull { it.name == quoteDataSource },
        voltpayLookup = VoltpayLookupRecipientForm(
            voltTag = voltTag,
            email = lookupEmail,
            mobile = lookupMobile,
            note = lookupNote
        ),
        bankDetails = BankRecipientForm(
            fullName = bankFullName,
            iban = bankIban,
            bic = bankBic,
            swift = bankSwift,
            bankName = bankName,
            bankAddress = bankAddress,
            bankCity = bankCity,
            bankCountry = bankCountry,
            bankPostalCode = bankPostalCode
        ),
        documentUpload = DocumentRecipientForm(
            fileRef = documentFileRef,
            docType = documentType,
            note = documentNote
        ),
        emailRequest = EmailRequestRecipientForm(
            email = requestEmail,
            fullName = requestFullName,
            note = requestNote
        ),
        updatedAtMs = updatedAtMs
    )
}

private fun FxQuote.toJsonPayload(): String {
    return gson.toJson(
        FxQuotePayload(
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            sourceAmount = sourceAmount,
            rate = rate,
            recipientAmount = recipientAmount,
            fees = fees.map { fee ->
                FxQuoteFeePayload(
                    label = fee.label,
                    amount = fee.amount,
                    code = fee.code
                )
            },
            guaranteeSeconds = guaranteeSeconds,
            arrivalSeconds = arrivalSeconds,
            rateSource = rateSource,
            updatedAtMs = updatedAtMs
        )
    )
}

private fun String?.toQuoteOrNull(): FxQuote? {
    if (this.isNullOrBlank()) return null
    return runCatching {
        val payload = gson.fromJson(this, FxQuotePayload::class.java)
        FxQuote(
            sourceCurrency = payload.sourceCurrency,
            targetCurrency = payload.targetCurrency,
            sourceAmount = payload.sourceAmount,
            rate = payload.rate,
            recipientAmount = payload.recipientAmount,
            fees = payload.fees.map { fee ->
                FxFeeLine(
                    label = fee.label,
                    amount = fee.amount,
                    code = fee.code
                )
            },
            guaranteeSeconds = payload.guaranteeSeconds,
            arrivalSeconds = payload.arrivalSeconds,
            rateSource = payload.rateSource,
            updatedAtMs = payload.updatedAtMs
        )
    }.getOrNull()
}

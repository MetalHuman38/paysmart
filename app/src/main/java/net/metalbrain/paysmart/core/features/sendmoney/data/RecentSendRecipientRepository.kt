package net.metalbrain.paysmart.core.features.sendmoney.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.core.features.sendmoney.domain.BankRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.EmailRequestRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecentSendRecipient
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm
import net.metalbrain.paysmart.room.dao.SendMoneyRecentRecipientDao
import net.metalbrain.paysmart.room.entity.SendMoneyRecentRecipientEntity
import java.util.Locale

@Singleton
class RecentSendRecipientRepository @Inject constructor(
    private val dao: SendMoneyRecentRecipientDao
) {
    fun observeRecentByUserId(
        userId: String,
        limit: Int = DEFAULT_LIMIT
    ): Flow<List<RecentSendRecipient>> {
        val safeLimit = limit.coerceAtLeast(1)
        return dao.observeRecentByUserId(userId = userId, limit = safeLimit)
            .map { entities -> entities.map(SendMoneyRecentRecipientEntity::toDomain) }
    }

    suspend fun getByKey(
        userId: String,
        recipientKey: String
    ): RecentSendRecipient? {
        return dao.getByUserIdAndKey(
            userId = userId,
            recipientKey = recipientKey
        )?.toDomain()
    }

    suspend fun recordFromDraft(
        userId: String,
        draft: SendMoneyRecipientDraft
    ) {
        val recipient = draft.toRecentSendRecipientOrNull() ?: return
        dao.upsert(recipient.toEntity(userId))
    }

    companion object {
        const val DEFAULT_LIMIT: Int = 6
    }
}

private fun SendMoneyRecipientDraft.toRecentSendRecipientOrNull(): RecentSendRecipient? {
    val normalized = normalized()
    return when (normalized.selectedMethod) {
        RecipientMethod.VOLTPAY_LOOKUP -> {
            val handle = listOf(
                normalized.voltpayLookup.voltTag.takeIf { it.isNotBlank() }?.let { "@$it" },
                normalized.voltpayLookup.email.takeIf { it.isNotBlank() }?.substringBefore("@"),
                normalized.voltpayLookup.mobile.takeIf { it.isNotBlank() }?.maskTrailing(4)
            ).firstOrNull()
                ?: return null

            val subtitle = listOf(
                normalized.voltpayLookup.email.takeIf { it.isNotBlank() },
                normalized.voltpayLookup.mobile.takeIf { it.isNotBlank() }?.maskTrailing(4),
                normalized.targetCurrency.takeIf { it.isNotBlank() }
            ).firstOrNull().orEmpty()

            RecentSendRecipient(
                recipientKey = recentRecipientKey(normalized),
                selectedMethod = normalized.selectedMethod,
                sourceCurrency = normalized.sourceCurrency,
                targetCurrency = normalized.targetCurrency,
                displayName = handle,
                subtitle = subtitle,
                voltpayLookup = normalized.voltpayLookup,
                updatedAtMs = normalized.updatedAtMs
            )
        }

        RecipientMethod.BANK_DETAILS -> {
            val fullName = normalized.bankDetails.fullName.takeIf { it.isNotBlank() } ?: return null
            val subtitle = listOf(
                normalized.bankDetails.bankName.takeIf { it.isNotBlank() },
                normalized.bankDetails.bankCountry.takeIf { it.isNotBlank() },
                normalized.bankDetails.iban.takeIf { it.isNotBlank() }?.maskTrailing(4)
            ).firstOrNull().orEmpty()

            RecentSendRecipient(
                recipientKey = recentRecipientKey(normalized),
                selectedMethod = normalized.selectedMethod,
                sourceCurrency = normalized.sourceCurrency,
                targetCurrency = normalized.targetCurrency,
                displayName = fullName,
                subtitle = subtitle,
                bankDetails = normalized.bankDetails,
                updatedAtMs = normalized.updatedAtMs
            )
        }

        RecipientMethod.EMAIL_REQUEST -> {
            val email = normalized.emailRequest.email.takeIf { it.isNotBlank() } ?: return null
            val displayName = normalized.emailRequest.fullName
                .takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")

            RecentSendRecipient(
                recipientKey = recentRecipientKey(normalized),
                selectedMethod = normalized.selectedMethod,
                sourceCurrency = normalized.sourceCurrency,
                targetCurrency = normalized.targetCurrency,
                displayName = displayName,
                subtitle = email,
                emailRequest = normalized.emailRequest,
                updatedAtMs = normalized.updatedAtMs
            )
        }

        RecipientMethod.DOCUMENT_UPLOAD -> null
    }
}

private fun RecentSendRecipient.toEntity(userId: String): SendMoneyRecentRecipientEntity {
    return SendMoneyRecentRecipientEntity(
        userId = userId,
        recipientKey = recipientKey,
        selectedMethod = selectedMethod.storageKey,
        sourceCurrency = sourceCurrency,
        targetCurrency = targetCurrency,
        displayName = displayName,
        subtitle = subtitle,
        voltTag = voltpayLookup.voltTag,
        lookupEmail = voltpayLookup.email,
        lookupMobile = voltpayLookup.mobile,
        bankFullName = bankDetails.fullName,
        bankIban = bankDetails.iban,
        bankBic = bankDetails.bic,
        bankSwift = bankDetails.swift,
        bankName = bankDetails.bankName,
        bankAddress = bankDetails.bankAddress,
        bankCity = bankDetails.bankCity,
        bankCountry = bankDetails.bankCountry,
        bankPostalCode = bankDetails.bankPostalCode,
        requestEmail = emailRequest.email,
        requestFullName = emailRequest.fullName,
        updatedAtMs = updatedAtMs
    )
}

private fun SendMoneyRecentRecipientEntity.toDomain(): RecentSendRecipient {
    return RecentSendRecipient(
        recipientKey = recipientKey,
        selectedMethod = RecipientMethod.fromStorage(selectedMethod),
        sourceCurrency = sourceCurrency,
        targetCurrency = targetCurrency,
        displayName = displayName,
        subtitle = subtitle,
        voltpayLookup = VoltpayLookupRecipientForm(
            voltTag = voltTag,
            email = lookupEmail,
            mobile = lookupMobile
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
        emailRequest = EmailRequestRecipientForm(
            email = requestEmail,
            fullName = requestFullName
        ),
        updatedAtMs = updatedAtMs
    )
}

private fun recentRecipientKey(draft: SendMoneyRecipientDraft): String {
    return when (draft.selectedMethod) {
        RecipientMethod.VOLTPAY_LOOKUP -> {
            val value = draft.voltpayLookup.voltTag.takeIf { it.isNotBlank() }
                ?: draft.voltpayLookup.email.takeIf { it.isNotBlank() }
                ?: draft.voltpayLookup.mobile.onlyDigits()
            "lookup:${value.orEmpty().trim().lowercase(Locale.US)}"
        }

        RecipientMethod.BANK_DETAILS -> {
            val value = draft.bankDetails.iban.takeIf { it.isNotBlank() }
                ?: "${draft.bankDetails.fullName}|${draft.bankDetails.bankName}"
            "bank:${value.trim().uppercase(Locale.US)}"
        }

        RecipientMethod.EMAIL_REQUEST -> {
            "email:${draft.emailRequest.email.trim().lowercase(Locale.US)}"
        }

        RecipientMethod.DOCUMENT_UPLOAD -> {
            "document:${draft.documentUpload.fileRef.trim().lowercase(Locale.US)}"
        }
    }
}

private fun String.maskTrailing(visibleCount: Int): String {
    val normalized = trim()
    if (normalized.isBlank() || normalized.length <= visibleCount) {
        return normalized
    }
    return buildString {
        append("*".repeat(normalized.length - visibleCount))
        append(normalized.takeLast(visibleCount))
    }
}

private fun String.onlyDigits(): String {
    return filter(Char::isDigit)
}

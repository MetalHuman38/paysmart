package net.metalbrain.paysmart.core.features.invoicing.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoicePaymentMethod
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.room.dao.InvoiceProfileDraftDao
import net.metalbrain.paysmart.room.entity.InvoiceProfileDraftEntity

/**
 * Repository responsible for managing [InvoiceProfileDraft] data.
 *
 * This class provides an abstraction layer over the [InvoiceProfileDraftDao] to handle
 * the persistence and retrieval of invoice profile drafts, performing the necessary
 * mapping between domain models and database entities.
 */
@Singleton
class InvoiceProfileDraftRepository @Inject constructor(
    private val dao: InvoiceProfileDraftDao
) {
    fun observeByUserId(userId: String): Flow<InvoiceProfileDraft?> {
        return dao.observeByUserId(userId).map { entity -> entity?.toDomain() }
    }

    suspend fun getByUserId(userId: String): InvoiceProfileDraft? {
        return dao.getByUserId(userId)?.toDomain()
    }

    suspend fun upsert(userId: String, draft: InvoiceProfileDraft) {
        dao.upsert(draft.normalized().toEntity(userId))
    }
}

private fun InvoiceProfileDraft.toEntity(userId: String): InvoiceProfileDraftEntity {
    return InvoiceProfileDraftEntity(
        userId = userId,
        fullName = fullName,
        address = address,
        badgeNumber = badgeNumber,
        badgeExpiryDate = badgeExpiryDate,
        utrNumber = utrNumber,
        email = email,
        contactPhone = contactPhone,
        paymentMethod = paymentMethod.storageKey,
        accountNumber = accountNumber,
        sortCode = sortCode,
        paymentInstructions = paymentInstructions,
        defaultHourlyRateInput = defaultHourlyRateInput,
        declaration = declaration,
        updatedAtMs = updatedAtMs
    )
}

private fun InvoiceProfileDraftEntity.toDomain(): InvoiceProfileDraft {
    return InvoiceProfileDraft(
        fullName = fullName,
        address = address,
        badgeNumber = badgeNumber,
        badgeExpiryDate = badgeExpiryDate,
        utrNumber = utrNumber,
        email = email,
        contactPhone = contactPhone,
        paymentMethod = InvoicePaymentMethod.fromStorage(paymentMethod),
        accountNumber = accountNumber,
        sortCode = sortCode,
        paymentInstructions = paymentInstructions,
        defaultHourlyRateInput = defaultHourlyRateInput,
        declaration = declaration,
        updatedAtMs = updatedAtMs
    )
}

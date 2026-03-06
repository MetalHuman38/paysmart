package net.metalbrain.paysmart.core.features.invoicing.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceShiftDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.room.doa.InvoiceWeeklyDraftDao
import net.metalbrain.paysmart.room.entity.InvoiceWeeklyDraftEntity

/**
 * Repository responsible for managing [InvoiceWeeklyDraft] persistence and retrieval.
 *
 * This class acts as a bridge between the data layer ([InvoiceWeeklyDraftDao]) and the domain layer,
 * ensuring that draft data is properly normalized and converted between [InvoiceWeeklyDraftEntity]
 * and [InvoiceWeeklyDraft].
 *
 * @property dao The Data Access Object for invoice weekly drafts.
 */
@Singleton
class InvoiceWeeklyDraftRepository @Inject constructor(
    private val dao: InvoiceWeeklyDraftDao
) {
    fun observeByUserId(userId: String): Flow<InvoiceWeeklyDraft?> {
        return dao.observeByUserId(userId).map { entity -> entity?.toDomain() }
    }

    suspend fun getByUserId(userId: String): InvoiceWeeklyDraft? {
        return dao.getByUserId(userId)?.toDomain()
    }

    suspend fun upsert(userId: String, draft: InvoiceWeeklyDraft) {
        dao.upsert(draft.normalized().withFullWeek().toEntity(userId))
    }
}

private val gson = Gson()

private fun InvoiceWeeklyDraft.toEntity(userId: String): InvoiceWeeklyDraftEntity {
    return InvoiceWeeklyDraftEntity(
        userId = userId,
        selectedVenueId = selectedVenueId,
        invoiceDate = invoiceDate,
        weekEndingDate = weekEndingDate,
        shiftsJson = gson.toJson(shifts),
        hourlyRateInput = hourlyRateInput,
        updatedAtMs = updatedAtMs
    )
}

private fun InvoiceWeeklyDraftEntity.toDomain(): InvoiceWeeklyDraft {
    return InvoiceWeeklyDraft(
        selectedVenueId = selectedVenueId,
        invoiceDate = invoiceDate,
        weekEndingDate = weekEndingDate,
        shifts = parseShifts(shiftsJson),
        hourlyRateInput = hourlyRateInput,
        updatedAtMs = updatedAtMs
    ).withFullWeek()
}

private fun parseShifts(raw: String): List<InvoiceShiftDraft> {
    if (raw.isBlank()) return InvoiceWeeklyDraft.defaultWeekShifts()
    return runCatching {
        val type = object : TypeToken<List<InvoiceShiftDraft>>() {}.type
        gson.fromJson<List<InvoiceShiftDraft>>(raw, type).orEmpty()
    }.getOrDefault(InvoiceWeeklyDraft.defaultWeekShifts())
}

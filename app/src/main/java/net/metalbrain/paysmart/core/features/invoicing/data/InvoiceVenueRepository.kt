package net.metalbrain.paysmart.core.features.invoicing.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.room.dao.InvoiceVenueDao
import net.metalbrain.paysmart.room.entity.InvoiceVenueEntity



/**
 * Repository responsible for managing [InvoiceVenueDraft] data.
 *
 * This class serves as an abstraction layer over the [InvoiceVenueDao], providing
 * domain models to the upper layers and handling the conversion between entities and drafts.
 */
@Singleton
class InvoiceVenueRepository @Inject constructor(
    private val dao: InvoiceVenueDao
) {
    fun observeByUserId(userId: String): Flow<List<InvoiceVenueDraft>> {
        return dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun listByUserId(userId: String): List<InvoiceVenueDraft> {
        return dao.listByUserId(userId).map { it.toDomain() }
    }

    suspend fun upsert(userId: String, venue: InvoiceVenueDraft) {
        dao.upsert(venue.normalized().toEntity(userId))
    }

    suspend fun delete(userId: String, venueId: String) {
        dao.deleteByUserIdAndVenueId(userId, venueId)
    }
}

private fun InvoiceVenueDraft.toEntity(userId: String): InvoiceVenueEntity {
    return InvoiceVenueEntity(
        venueId = venueId,
        userId = userId,
        venueName = venueName,
        venueAddress = venueAddress,
        defaultHourlyRateInput = defaultHourlyRateInput,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun InvoiceVenueEntity.toDomain(): InvoiceVenueDraft {
    return InvoiceVenueDraft(
        venueId = venueId,
        venueName = venueName,
        venueAddress = venueAddress,
        defaultHourlyRateInput = defaultHourlyRateInput,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

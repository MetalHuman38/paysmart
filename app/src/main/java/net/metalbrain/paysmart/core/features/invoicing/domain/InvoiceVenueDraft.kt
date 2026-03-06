package net.metalbrain.paysmart.core.features.invoicing.domain

/**
 * Represents a draft version of an invoice venue.
 *
 * This data class is used to hold temporary state while creating or editing venue details
 * before they are persisted. It includes validation logic and a normalization utility.
 *
 * @property venueId The unique identifier of the venue.
 * @property venueName The display name of the venue.
 * @property venueAddress The physical or billing address of the venue.
 * @property defaultHourlyRateInput The raw string input representing the default hourly rate.
 * @property createdAtMs The timestamp when the draft was first created, in milliseconds.
 * @property updatedAtMs The timestamp when the draft was last modified, in milliseconds.
 */
data class InvoiceVenueDraft(
    val venueId: String = "",
    val venueName: String = "",
    val venueAddress: String = "",
    val defaultHourlyRateInput: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = venueName.isNotBlank()

    fun normalized(nowMs: Long = System.currentTimeMillis()): InvoiceVenueDraft {
        return copy(
            venueId = venueId.trim(),
            venueName = venueName.trim(),
            venueAddress = venueAddress.trim(),
            defaultHourlyRateInput = defaultHourlyRateInput.trim(),
            updatedAtMs = nowMs
        )
    }
}

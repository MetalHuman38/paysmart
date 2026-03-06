package net.metalbrain.paysmart.core.features.invoicing.domain

/**
 * Represents the detailed information of an invoice, including financial totals,
 * associated profile details, venue information, and the weekly shift breakdown.
 *
 * @property invoiceId The unique identifier of the invoice.
 * @property invoiceNumber The human-readable reference number for the invoice.
 * @property status The current status of the invoice (e.g., "Pending", "Paid").
 * @property sequenceNumber The chronological sequence number of the invoice.
 * @property totalHours The total number of hours worked across all shifts.
 * @property hourlyRate The rate charged per hour.
 * @property subtotalMinor The total amount in the minor unit of the currency (e.g., pence or cents).
 * @property currency The ISO currency code (e.g., "GBP").
 * @property venueName The name of the venue where the work was performed.
 * @property weekEndingDate The date representing the end of the work week.
 * @property createdAtMs The timestamp when the invoice was created, in milliseconds.
 * @property pdf The current generated-PDF state for this invoice.
 * @property profile Detailed personal and payment information of the service provider.
 * @property venue Detailed information about the venue.
 * @property weekly The specific weekly breakdown of shifts and dates.
 */
data class InvoiceDetail(
    val invoiceId: String,
    val invoiceNumber: String,
    val status: String,
    val sequenceNumber: Int,
    val totalHours: Double,
    val hourlyRate: Double,
    val subtotalMinor: Int,
    val currency: String,
    val venueName: String,
    val weekEndingDate: String,
    val createdAtMs: Long,
    val pdf: InvoicePdfDocument,
    val profile: InvoiceDetailProfile,
    val venue: InvoiceDetailVenue,
    val weekly: InvoiceDetailWeekly
)

data class InvoicePdfDocument(
    val status: String,
    val fileName: String,
    val contentType: String,
    val templateVersion: String,
    val objectPath: String? = null,
    val sizeBytes: Int? = null,
    val generatedAtMs: Long? = null,
    val error: String? = null
)

data class InvoiceDetailProfile(
    val fullName: String,
    val address: String,
    val badgeNumber: String,
    val badgeExpiryDate: String,
    val utrNumber: String,
    val email: String,
    val contactPhone: String,
    val paymentMethod: String,
    val accountNumber: String,
    val sortCode: String,
    val paymentInstructions: String,
    val declaration: String
)

data class InvoiceDetailVenue(
    val venueId: String,
    val venueName: String,
    val venueAddress: String
)

data class InvoiceDetailWeekly(
    val invoiceDate: String,
    val weekEndingDate: String,
    val hourlyRateInput: String,
    val shifts: List<InvoiceDetailShift>
)

data class InvoiceDetailShift(
    val dayLabel: String,
    val workDate: String,
    val hoursInput: String
)

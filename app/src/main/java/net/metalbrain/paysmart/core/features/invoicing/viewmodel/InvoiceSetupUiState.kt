package net.metalbrain.paysmart.core.features.invoicing.viewmodel

import net.metalbrain.paysmart.core.auth.AddressLookupResult
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceSummary
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceFinalizeResult
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceShiftDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Represents the sequential steps in the invoice setup wizard.
 */
enum class InvoiceSetupStep {
    PROFILE,
    VENUES,
    SUMMARY
}

/**
 * Represents the UI state for the invoice setup flow.
 *
 * @property userId The unique identifier of the user setting up their invoice details.
 * @property step The current step in the setup process (e.g., Profile, Venues, or Summary).
 * @property profileDraft The current draft of the user's personal invoicing profile information.
 * @property venues The list of venues that have been added to the setup.
 * @property venueNameInput The current text input for a new venue's name.
 * @property venueAddressInput The current text input for a new venue's address.
 * @property venueCountryInput The current ISO country code input for a new venue. Defaults to "GB".
 * @property venueRateInput The current text input for a new venue's hourly rate.
 * @property weeklyDraft The current draft of the recurring weekly invoice shifts.
 * @property isHydrating Indicates if the initial data is being loaded from a persistent store.
 * @property isPersisting Indicates if the state is currently being saved to the backend or local storage.
 * @property isAddressSearching Indicates if an address lookup operation is currently in progress.
 * @property suggestedVenueAddress The result of an address lookup to be used for auto-completion.
 * @property error An optional error message to be displayed to the user.
 * @property infoMessage An optional informative message to be displayed to the user.
 * @property weeklyRows Provides a complete list of shifts for a full week based on the current [weeklyDraft].
 * @property canContinue Determines if the user has met the requirements to proceed to the next step.
 */
data class InvoiceSetupUiState(
    val userId: String? = null,
    val step: InvoiceSetupStep = InvoiceSetupStep.PROFILE,
    val profileDraft: InvoiceProfileDraft = InvoiceProfileDraft(),
    val venues: List<InvoiceVenueDraft> = emptyList(),
    val venueNameInput: String = "",
    val venueAddressInput: String = "",
    val venueCountryInput: String = "GB",
    val venueRateInput: String = "",
    val weeklyDraft: InvoiceWeeklyDraft = InvoiceWeeklyDraft(),
    val isHydrating: Boolean = true,
    val isPersisting: Boolean = false,
    val isFinalizing: Boolean = false,
    val isAddressSearching: Boolean = false,
    val suggestedVenueAddress: AddressLookupResult? = null,
    val finalizedInvoice: InvoiceFinalizeResult? = null,
    val finalizedInvoices: List<InvoiceSummary> = emptyList(),
    val isInvoiceHistoryLoading: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val weeklyRows: List<InvoiceShiftDraft>
        get() = weeklyDraft.withFullWeek().shifts

    val effectiveSelectedVenueId: String
        get() = weeklyDraft.selectedVenueId
            .trim()
            .ifBlank { venues.firstOrNull()?.venueId.orEmpty() }

    val hasWorkedShift: Boolean
        get() = weeklyRows.any { row ->
            (row.hoursInput.toDoubleOrNull() ?: 0.0) > 0.0
        }

    val hasValidInvoiceDate: Boolean
        get() = weeklyDraft.invoiceDate.isIsoLocalDate()

    val hasValidWeekEndingDate: Boolean
        get() = weeklyDraft.weekEndingDate.isIsoLocalDate()

    val hasValidHourlyRate: Boolean
        get() = (weeklyDraft.hourlyRateInput.toDoubleOrNull() ?: 0.0) > 0.0

    val canProceedToVenue: Boolean
        get() = profileDraft.isValid

    val canProceedToWeekly: Boolean
        get() = profileDraft.isValid && venues.isNotEmpty()

    val canFinalize: Boolean
        get() = effectiveSelectedVenueId.isNotBlank() &&
            hasValidInvoiceDate &&
            hasValidWeekEndingDate &&
            hasValidHourlyRate &&
            hasWorkedShift

    val canContinue: Boolean
        get() = when (step) {
            InvoiceSetupStep.PROFILE -> profileDraft.isValid
            InvoiceSetupStep.VENUES -> venues.isNotEmpty()
            InvoiceSetupStep.SUMMARY -> canFinalize
        }
}

private fun String.isIsoLocalDate(): Boolean {
    val normalized = trim()
    if (normalized.isBlank()) return false
    return try {
        LocalDate.parse(normalized)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}

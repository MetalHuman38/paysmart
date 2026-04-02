package net.metalbrain.paysmart.core.features.invoicing.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.AddressLookupPayload
import net.metalbrain.paysmart.core.auth.AddressResolverPolicyHandler
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceProfileDraftRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceFinalizeRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceSetupPreferenceRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceSetupSelection
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceReadRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceVenueRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceWeeklyDraftRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.toDynamicInvoice
import net.metalbrain.paysmart.core.features.invoicing.domain.toLegacyWeeklyDraft
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.core.invoice.calculation.InvoiceCalculations
import net.metalbrain.paysmart.core.invoice.factory.InvoiceFactory
import net.metalbrain.paysmart.core.invoice.model.FieldType
import net.metalbrain.paysmart.core.invoice.model.Invoice
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.core.invoice.model.LineItem
import net.metalbrain.paysmart.core.invoice.model.Profession
import net.metalbrain.paysmart.core.invoice.model.doubleValue
import net.metalbrain.paysmart.core.invoice.template.InvoiceTemplateCatalog
import java.util.UUID

/**
 * ViewModel responsible for managing the state and logic of the invoice setup flow.
 *
 * This ViewModel handles the orchestration of invoice profile data, venue selections, and
 * weekly shift drafts. It synchronizes local UI state with persistent storage through
 * various repositories and provides methods for address resolution and shift calculations.
 *
 * Key responsibilities:
 * - Observing and hydrating invoice-related drafts (profile, venues, weekly shifts) for the current user.
 * - Managing the multistep navigation state of the invoice setup process.
 * - Handling real-time updates and persistence of weekly shift data (hours, dates, and rates).
 * - Integrating with [AddressResolverPolicyHandler] to search and apply venue addresses.
 *
 * @property authRepository Provides access to current user session and authentication details.
 * @property profileRepository Manages persistence for the user's invoice profile draft.
 * @property venueRepository Manages the list of venues associated with the user.
 * @property weeklyDraftRepository Manages the persistence of weekly invoice drafts.
 * @property addressResolverPolicyHandler Handles external address lookup and validation logic.
 */
@HiltViewModel
class InvoiceSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileCacheRepository: UserProfileCacheRepository,
    private val profileRepository: InvoiceProfileDraftRepository,
    private val venueRepository: InvoiceVenueRepository,
    private val weeklyDraftRepository: InvoiceWeeklyDraftRepository,
    private val finalizeRepository: InvoiceFinalizeRepository,
    private val readRepository: InvoiceReadRepository,
    private val setupPreferenceRepository: InvoiceSetupPreferenceRepository,
    private val addressResolverPolicyHandler: AddressResolverPolicyHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceSetupUiState())
    val uiState: StateFlow<InvoiceSetupUiState> = _uiState.asStateFlow()

    init {
        observeDrafts()
        refreshFinalizedInvoices()
    }

    fun setStep(step: InvoiceSetupStep) {
        _uiState.update { it.copy(step = step, error = null) }
    }

    fun setFormStep(step: InvoiceFormStep) {
        _uiState.update { it.copy(formStep = step, error = null, infoMessage = null) }
    }

    fun selectProfession(profession: Profession) {
        applyTemplateSelection(
            templateId = profession.templateId,
            profession = profession,
            nextFormStep = InvoiceFormStep.INVOICE_INFO
        )
    }

    fun applyTemplate(templateId: String) {
        val currentProfession = _uiState.value.selectedProfession
        val templateProfession = InvoiceTemplateCatalog.template(templateId)
            ?.professionId
            ?.let(InvoiceTemplateCatalog::profession)
        applyTemplateSelection(
            templateId = templateId,
            profession = currentProfession?.takeIf { it.templateId == templateId } ?: templateProfession,
            nextFormStep = _uiState.value.formStep
        )
    }

    fun updateSectionField(sectionId: String, fieldKey: String, rawValue: Any?) {
        val field = _uiState.value.draftInvoice.sections
            .firstOrNull { it.id == sectionId }
            ?.fields
            ?.firstOrNull { it.key == fieldKey }
            ?: return
        updateDraftSectionField(
            sectionId = sectionId,
            fieldKey = fieldKey,
            value = field.coerceInputValue(rawValue),
            persistWeekly = sectionId == "invoice_info"
        )
    }

    fun updateLineItemField(index: Int, fieldKey: String, rawValue: Any?) {
        val field = _uiState.value.draftInvoice.lineItems
            .getOrNull(index)
            ?.fields
            ?.firstOrNull { it.key == fieldKey }
            ?: return
        updateDraftInvoice(persistWeekly = true) { invoice ->
            invoice.withLineItemFieldValueAt(
                index = index,
                fieldKey = fieldKey,
                value = field.coerceInputValue(rawValue)
            )
        }
    }

    fun addLineItem() {
        val current = _uiState.value
        val template = current.selectedTemplate ?: return
        if (current.draftInvoice.lineItems.size >= MAX_DYNAMIC_LINE_ITEMS) {
            _uiState.update {
                it.copy(
                    error = "You can add up to $MAX_DYNAMIC_LINE_ITEMS line items in the current invoice flow"
                )
            }
            return
        }

        val defaultRate = current.draftInvoice.lineItems.firstOrNull()
            ?.fields
            ?.firstOrNull { field -> field.key == InvoiceFieldKeys.LINE_RATE }
            ?.value
            ?: current.draftInvoice.sections
                .firstOrNull { section -> section.id == "worker_details" }
                ?.fields
                ?.firstOrNull { field -> field.key == InvoiceFieldKeys.DEFAULT_RATE }
                ?.value

        val nextLineItem = LineItem(
            id = "line_${UUID.randomUUID()}",
            fields = template.lineItemFields.map { field ->
                when (field.key) {
                    InvoiceFieldKeys.LINE_RATE -> field.copy(value = defaultRate)
                    else -> field.copy(value = null)
                }
            }
        )

        updateDraftInvoice(persistWeekly = true) { invoice ->
            invoice.copy(
                lineItems = InvoiceCalculations.withComputedAmounts(invoice.lineItems + nextLineItem)
            )
        }
    }

    fun removeLineItem(lineItemId: String) {
        val current = _uiState.value
        if (current.draftInvoice.lineItems.size <= 1) {
            _uiState.update {
                it.copy(error = "At least one line item is required for invoice review")
            }
            return
        }
        updateDraftInvoice(persistWeekly = true) { invoice ->
            invoice.copy(
                lineItems = InvoiceCalculations.withComputedAmounts(
                    invoice.lineItems.filterNot { lineItem -> lineItem.id == lineItemId }
                )
            )
        }
    }

    fun goToNextFormStep() {
        val current = _uiState.value
        if (current.formStep == InvoiceFormStep.REVIEW) {
            finalizeInvoice()
            return
        }

        val validationError = current.validationErrorForCurrentStep()
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError, infoMessage = null) }
            return
        }

        current.formStep.nextProgressiveStep()?.let { nextStep ->
            _uiState.update { it.copy(formStep = nextStep, error = null, infoMessage = null) }
            saveDraftInternal(showFeedback = false)
        }
    }

    fun goToPreviousFormStep() {
        _uiState.value.formStep.previousProgressiveStep()?.let { previousStep ->
            _uiState.update { it.copy(formStep = previousStep, error = null, infoMessage = null) }
        }
    }

    fun saveDraft() {
        saveDraftInternal(showFeedback = true)
    }

    fun updateProfileFullName(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_NAME, value)
    }

    fun updateProfileAddress(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_ADDRESS, value)
    }

    fun updateProfileBadgeNumber(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_BADGE_NUMBER, value)
    }

    fun updateProfileBadgeExpiryDate(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_BADGE_EXPIRY, value)
    }

    fun updateProfileUtrNumber(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_UTR, value)
    }

    fun updateProfileEmail(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_EMAIL, value)
    }

    fun updateProfileContactPhone(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.WORKER_PHONE, value)
    }

    fun updateProfileAccountNumber(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.PAYMENT_ACCOUNT_NUMBER, value)
    }

    fun updateProfileSortCode(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.PAYMENT_SORT_CODE, value)
    }

    fun updateProfilePaymentInstructions(value: String) {
        updateDraftSectionField("worker_details", InvoiceFieldKeys.PAYMENT_INSTRUCTIONS, value)
    }

    fun updateProfileDefaultHourlyRate(value: String) {
        updateDraftSectionField(
            sectionId = "worker_details",
            fieldKey = InvoiceFieldKeys.DEFAULT_RATE,
            value = sanitizeDecimal(value)
        )
    }

    fun saveProfileDraft() {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val draft = _uiState.value.profileDraft.normalized()
        _uiState.update { it.copy(isPersisting = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            runCatching { profileRepository.upsert(userId, draft) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            infoMessage = "Invoice profile saved"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to save invoice profile"
                        )
                    }
                }
        }
    }

    fun proceedToVenue(onSuccess: () -> Unit) {
        val current = _uiState.value
        val userId = current.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val draft = current.profileDraft.normalized()
        if (!draft.isValid) {
            _uiState.update { it.copy(error = "Complete your worker profile before continuing") }
            return
        }

        _uiState.update { it.copy(isPersisting = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            runCatching { profileRepository.upsert(userId, draft) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            infoMessage = "Invoice profile saved",
                            formStep = InvoiceFormStep.CLIENT_DETAILS
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to save invoice profile"
                        )
                    }
                }
        }
    }

    fun proceedToWeekly(onSuccess: () -> Unit) {
        val current = _uiState.value
        val userId = current.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val draft = current.profileDraft.normalized()
        if (!draft.isValid) {
            _uiState.update { it.copy(error = "Complete and save your invoice profile first") }
            return
        }

        if (current.venues.isEmpty()) {
            _uiState.update { it.copy(error = "Add at least one venue before continuing") }
            return
        }

        _uiState.update { it.copy(isPersisting = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            runCatching { profileRepository.upsert(userId, draft) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            infoMessage = "Invoice profile saved",
                            formStep = InvoiceFormStep.WORK_DETAILS
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to save invoice profile"
                        )
                    }
                }
        }
    }

    private fun applyTemplateSelection(
        templateId: String,
        profession: Profession?,
        nextFormStep: InvoiceFormStep
    ) {
        val currentState = _uiState.value
        val nextInvoice = applyTemplateToInvoice(
            invoice = currentState.draftInvoice,
            templateId = templateId,
            professionId = profession?.id
        )
        _uiState.update {
            it.copy(
                draftInvoice = nextInvoice,
                selectedProfession = profession,
                formStep = nextFormStep,
                error = null,
                infoMessage = null
            )
        }
        viewModelScope.launch {
            setupPreferenceRepository.setSelection(
                professionId = profession?.id,
                templateId = templateId
            )
        }
        persistWeeklyDraft()
    }

    fun updateVenueNameInput(value: String) {
        _uiState.update { it.copy(venueNameInput = value, error = null, infoMessage = null) }
    }

    fun updateVenueRateInput(value: String) {
        _uiState.update { it.copy(venueRateInput = sanitizeDecimal(value), error = null, infoMessage = null) }
    }

    fun addVenue() {
        val current = _uiState.value
        val userId = current.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val venue = InvoiceVenueDraft(
            venueId = "venue_${UUID.randomUUID()}",
            venueName = current.venueNameInput,
            venueAddress = current.venueAddressInput,
            defaultHourlyRateInput = current.venueRateInput
        ).normalized()

        if (!venue.isValid) {
            _uiState.update { it.copy(error = "Venue name is required before saving") }
            return
        }

        _uiState.update { it.copy(isPersisting = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            runCatching { venueRepository.upsert(userId, venue) }
                .onSuccess {
                    val shouldApplyVenueRate = _uiState.value.weeklyDraft.hourlyRateInput.isBlank() &&
                        venue.defaultHourlyRateInput.isNotBlank()
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            venueNameInput = "",
                            venueAddressInput = "",
                            venueRateInput = "",
                            suggestedVenueAddress = null,
                            infoMessage = "Venue added"
                        )
                    }
                    selectVenue(venue.venueId)
                    if (shouldApplyVenueRate) {
                        updateHourlyRateInput(venue.defaultHourlyRateInput)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPersisting = false,
                            error = error.localizedMessage ?: "Unable to add venue"
                        )
                    }
                }
        }
    }

    fun selectVenue(venueId: String) {
        val venue = _uiState.value.venues.firstOrNull { it.venueId == venueId.trim() }
        val nextInvoice = syncInvoiceWithVenue(_uiState.value.draftInvoice, venue)
        _uiState.update {
            it.copy(
                selectedVenueId = venueId.trim(),
                draftInvoice = nextInvoice,
                error = null,
                infoMessage = null
            )
        }
        persistWeeklyDraft()
    }

    fun updateInvoiceDate(value: String) {
        updateDraftSectionField(
            sectionId = "invoice_info",
            fieldKey = InvoiceFieldKeys.INVOICE_DATE,
            value = value.trim(),
            persistWeekly = true
        )
    }

    fun updateWeekEndingDate(value: String) {
        updateDraftSectionField(
            sectionId = "invoice_info",
            fieldKey = InvoiceFieldKeys.WEEK_ENDING,
            value = value.trim(),
            persistWeekly = true
        )
    }

    fun updateHourlyRateInput(value: String) {
        val sanitized = sanitizeDecimal(value)
        updateDraftInvoice(
            persistWeekly = true
        ) { invoice ->
            invoice.withLineItemFieldValue(
                fieldKey = InvoiceFieldKeys.LINE_RATE,
                transform = { sanitized.toDoubleOrNull() }
            )
        }
    }

    fun updateShiftDate(dayLabel: String, value: String) {
        val index = _uiState.value.weeklyRows.indexOfFirst { shift -> sameDay(shift.dayLabel, dayLabel) }
        if (index < 0) return
        updateDraftInvoice(
            persistWeekly = true
        ) { invoice ->
            invoice.withLineItemFieldValueAt(
                index = index,
                fieldKey = InvoiceFieldKeys.LINE_DATE,
                value = value.trim()
            )
        }
    }

    fun updateShiftDateAt(index: Int, value: String) {
        val dayLabel = _uiState.value.weeklyRows.getOrNull(index)?.dayLabel ?: return
        updateShiftDate(dayLabel, value)
    }

    fun updateShiftHours(dayLabel: String, value: String) {
        val sanitized = sanitizeDecimal(value)
        val index = _uiState.value.weeklyRows.indexOfFirst { shift -> sameDay(shift.dayLabel, dayLabel) }
        if (index < 0) return
        updateDraftInvoice(
            persistWeekly = true
        ) { invoice ->
            invoice.withLineItemFieldValueAt(
                index = index,
                fieldKey = InvoiceFieldKeys.LINE_HOURS,
                value = sanitized.toDoubleOrNull()
            )
        }
    }

    fun updateShiftHoursAt(index: Int, value: String) {
        val dayLabel = _uiState.value.weeklyRows.getOrNull(index)?.dayLabel ?: return
        updateShiftHours(dayLabel, value)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshFinalizedInvoices(limit: Int = 10) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInvoiceHistoryLoading = true) }
            readRepository.listFinalized(limit)
                .onSuccess { invoices ->
                    _uiState.update {
                        it.copy(
                            isInvoiceHistoryLoading = false,
                            finalizedInvoices = invoices
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isInvoiceHistoryLoading = false,
                            error = error.localizedMessage ?: "Unable to load finalized invoices"
                        )
                    }
                }
        }
    }

    fun finalizeInvoice() {
        val current = _uiState.value
        logFinalizeDiagnostics("attempt", current)
        if (current.userId.isNullOrBlank()) {
            logFinalizeDiagnostics("blocked_missing_user", current)
            _uiState.update { it.copy(error = "Authentication required before invoice finalization") }
            return
        }

        val profile = current.profileDraft.normalized()
        if (!profile.isValid) {
            logFinalizeDiagnostics("blocked_invalid_profile", current)
            _uiState.update { it.copy(error = "Complete invoice profile details before finalizing") }
            return
        }

        val venue = resolveSelectedVenue(current)
        if (venue == null) {
            logFinalizeDiagnostics("blocked_missing_venue", current)
            _uiState.update { it.copy(error = "Select a venue before finalizing") }
            return
        }

        val weekly = current.weeklyDraft.normalized().withFullWeek()
        if (weekly.invoiceDate.isBlank() || weekly.weekEndingDate.isBlank()) {
            logFinalizeDiagnostics("blocked_missing_dates", current)
            _uiState.update { it.copy(error = "Invoice date and week ending date are required") }
            return
        }

        if ((weekly.hourlyRateInput.toDoubleOrNull() ?: 0.0) <= 0.0) {
            logFinalizeDiagnostics("blocked_invalid_rate", current)
            _uiState.update { it.copy(error = "Enter a valid hourly rate before finalizing") }
            return
        }

        if (weekly.shifts.none { row -> (row.hoursInput.toDoubleOrNull() ?: 0.0) > 0.0 }) {
            logFinalizeDiagnostics("blocked_missing_shift", current)
            _uiState.update { it.copy(error = "Add at least one worked shift before finalizing") }
            return
        }

        _uiState.update {
            it.copy(
                isFinalizing = true,
                error = null,
                infoMessage = null,
                finalizedInvoice = null
            )
        }

        viewModelScope.launch {
            finalizeRepository.finalize(profile, venue, weekly)
                .onSuccess { finalized ->
                    Log.d(
                        "InvoiceFinalizeDiag",
                        "success invoiceId=${finalized.invoiceId} invoiceNumber=${finalized.invoiceNumber}"
                    )
                    val nextDraft = buildNextWeeklyDraftAfterFinalize(
                        venueId = venue.venueId,
                        fallbackHourlyRate = weekly.hourlyRateInput
                    )
                    runCatching {
                        weeklyDraftRepository.upsert(current.userId, nextDraft)
                    }.onFailure { resetError ->
                        Log.w(
                            "InvoiceFinalizeDiag",
                            "post_finalize_draft_reset_failed message=${resetError.localizedMessage.orEmpty()}"
                        )
                    }
                    _uiState.update {
                        val nextDynamicInvoice = nextDraft.toDynamicInvoice(profile, venue)
                            .let { drafted ->
                                applyTemplateToInvoice(
                                    invoice = drafted,
                                    templateId = current.draftInvoice.templateId
                                        ?: "weekly_shift_worker_template",
                                    professionId = current.draftInvoice.professionId
                                )
                            }
                        it.copy(
                            isFinalizing = false,
                            draftInvoice = nextDynamicInvoice,
                            selectedVenueId = venue.venueId,
                            finalizedInvoice = finalized,
                            infoMessage = "Invoice ${finalized.invoiceNumber} finalized"
                        )
                    }
                    refreshFinalizedInvoices()
                }
                .onFailure { error ->
                    Log.d(
                        "InvoiceFinalizeDiag",
                        "failure message=${error.localizedMessage ?: "Unable to finalize invoice"}"
                    )
                    _uiState.update {
                        it.copy(
                            isFinalizing = false,
                            error = error.localizedMessage ?: "Unable to finalize invoice"
                        )
                    }
                }
        }
    }

    fun updateVenueAddressInput(value: String) {
        _uiState.update {
            it.copy(
                venueAddressInput = value,
                suggestedVenueAddress = null,
                error = null
            )
        }
    }

    fun updateVenueCountryInput(value: String) {
        _uiState.update {
            it.copy(
                venueCountryInput = value.trim().uppercase().take(2).ifBlank { "GB" },
                suggestedVenueAddress = null,
                error = null
            )
        }
    }

    fun searchVenueAddress() {
        val current = _uiState.value
        if (current.venueAddressInput.isBlank()) {
            _uiState.update { it.copy(error = "Enter venue address to search") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddressSearching = true, error = null) }
            val session = runCatching { authRepository.getCurrentSessionOrThrow() }
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isAddressSearching = false,
                            error = error.localizedMessage ?: "Authentication required"
                        )
                    }
                    return@launch
                }

            val payload = AddressLookupPayload(
                line1 = current.venueAddressInput,
                city = "",
                stateOrRegion = "",
                postalCode = "",
                country = current.venueCountryInput
            )

            addressResolverPolicyHandler.resolveAddress(session.idToken, payload)
                .onSuccess { resolved ->
                    _uiState.update {
                        it.copy(
                            isAddressSearching = false,
                            suggestedVenueAddress = resolved,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAddressSearching = false,
                            suggestedVenueAddress = null,
                            error = error.localizedMessage ?: "Unable to resolve venue address"
                        )
                    }
                }
        }
    }

    fun applySuggestedVenueAddress() {
        val suggested = _uiState.value.suggestedVenueAddress ?: return
        _uiState.update {
            it.copy(
                venueAddressInput = suggested.fullAddressWithHouse.ifBlank { suggested.fullAddress },
                venueCountryInput = suggested.countryCode.trim().uppercase().take(2).ifBlank { it.venueCountryInput },
                suggestedVenueAddress = null,
                error = null
            )
        }
    }

    private fun observeDrafts() {
        val userId = authRepository.currentUser?.uid?.trim()
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(isHydrating = false, error = "Authentication required before invoice setup")
            }
            return
        }

        _uiState.update { it.copy(userId = userId, isHydrating = true, error = null) }
        viewModelScope.launch {
            combine(
                combine(
                    profileRepository.observeByUserId(userId),
                    userProfileCacheRepository.observeByUid(userId)
                ) { profile, cachedProfile ->
                    profile to cachedProfile
                },
                venueRepository.observeByUserId(userId),
                weeklyDraftRepository.observeByUserId(userId),
                setupPreferenceRepository.observeSelection()
            ) { profileAndCache, venues, weekly, selection ->
                InvoiceHydrationPayload(
                    profileAndCache = profileAndCache,
                    venues = venues,
                    weeklyDraft = weekly,
                    selection = selection
                )
            }.collectLatest { payload ->
                val (profileAndCache, venues, weekly, selection) = payload
                val (profile, cachedProfile) = profileAndCache
                val fallbackProfile = (profile ?: InvoiceProfileDraft())
                    .mergeMissingFrom(defaultProfileDraft(cachedProfile))
                val hydratedWeekly = (weekly ?: InvoiceWeeklyDraft()).withFullWeek()
                val selectedVenueId = hydratedWeekly.selectedVenueId
                    .takeIf { id -> venues.any { it.venueId == id } }
                    ?: venues.firstOrNull()?.venueId.orEmpty()
                val selectedVenue = venues.firstOrNull { it.venueId == selectedVenueId }
                val hydratedInvoice = buildHydratedDynamicInvoice(
                    profile = fallbackProfile,
                    venues = venues,
                    weekly = hydratedWeekly.copy(selectedVenueId = selectedVenueId),
                    selection = selection
                )
                _uiState.update { current ->
                    val currentInvoice = current.draftInvoice
                    val shouldPreserveCurrentDraft = !current.isHydrating &&
                        current.userId == userId &&
                        currentInvoice.isMeaningfullyFilled()
                    val nextInvoice = if (shouldPreserveCurrentDraft) {
                        syncInvoiceWithVenue(currentInvoice, selectedVenue)
                    } else {
                        syncInvoiceWithVenue(hydratedInvoice, selectedVenue)
                    }
                    current.copy(
                        selectedProfession = selection.professionId?.let(InvoiceTemplateCatalog::profession)
                            ?: nextInvoice.professionId?.let(InvoiceTemplateCatalog::profession),
                        venues = venues,
                        selectedVenueId = selectedVenueId,
                        draftInvoice = nextInvoice,
                        formStep = if (selection.professionId.isNullOrBlank() &&
                            !nextInvoice.isMeaningfullyFilled()
                        ) {
                            InvoiceFormStep.QUICK_START
                        } else if (current.formStep == InvoiceFormStep.QUICK_START) {
                            InvoiceFormStep.INVOICE_INFO
                        } else {
                            current.formStep
                        },
                        isHydrating = false,
                        isPersisting = false
                    )
                }
            }
        }
    }

    private fun updateDraftSectionField(
        sectionId: String,
        fieldKey: String,
        value: Any?,
        persistWeekly: Boolean = false
    ) {
        updateDraftInvoice(persistWeekly = persistWeekly) { invoice ->
            invoice.withSectionFieldValue(sectionId = sectionId, fieldKey = fieldKey, value = value)
        }
    }

    private fun updateDraftInvoice(
        persistWeekly: Boolean = false,
        transform: (Invoice) -> Invoice
    ) {
        _uiState.update { current ->
            itWithUpdatedDraft(current, transform(current.draftInvoice))
        }
        if (persistWeekly) {
            persistWeeklyDraft()
        }
    }

    private fun saveDraftInternal(showFeedback: Boolean) {
        val current = _uiState.value
        val userId = current.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val profileDraft = current.profileDraft.normalized()
        val weeklyDraft = current.draftInvoice
            .toLegacyWeeklyDraft(current.effectiveSelectedVenueId)
            .normalized()
            .withFullWeek()

        _uiState.update {
            it.copy(
                isPersisting = true,
                error = null,
                infoMessage = if (showFeedback) null else it.infoMessage
            )
        }
        viewModelScope.launch {
            val profileResult = runCatching { profileRepository.upsert(userId, profileDraft) }
            val weeklyResult = runCatching { weeklyDraftRepository.upsert(userId, weeklyDraft) }
            val failure = profileResult.exceptionOrNull() ?: weeklyResult.exceptionOrNull()

            _uiState.update {
                it.copy(
                    isPersisting = false,
                    error = failure?.localizedMessage,
                    infoMessage = if (failure == null && showFeedback) {
                        "Invoice draft saved"
                    } else {
                        it.infoMessage
                    }
                )
            }
        }
    }

    private fun itWithUpdatedDraft(current: InvoiceSetupUiState, invoice: Invoice): InvoiceSetupUiState {
        return current.copy(
            draftInvoice = InvoiceCalculations.recalculate(invoice),
            error = null,
            infoMessage = null,
            finalizedInvoice = null
        )
    }

    private fun persistWeeklyDraft() {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val next = _uiState.value.draftInvoice
            .toLegacyWeeklyDraft(_uiState.value.effectiveSelectedVenueId)
            .normalized()
            .withFullWeek()
        _uiState.update { it.copy(isPersisting = true) }
        viewModelScope.launch {
            runCatching { weeklyDraftRepository.upsert(userId, next) }
                .onSuccess { _uiState.update { it.copy(isPersisting = false) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isPersisting = false, error = error.localizedMessage ?: "Unable to save weekly draft")
                    }
                }
        }
    }

    private fun resolveSelectedVenue(state: InvoiceSetupUiState): InvoiceVenueDraft? {
        val selectedVenueId = state.effectiveSelectedVenueId.trim()
        if (selectedVenueId.isBlank()) return null
        return state.venues.firstOrNull { it.venueId == selectedVenueId }
    }

    private fun logFinalizeDiagnostics(stage: String, state: InvoiceSetupUiState) {
        val workedRows = state.weeklyRows
            .filter { (it.hoursInput.toDoubleOrNull() ?: 0.0) > 0.0 }
            .joinToString(separator = ";") { row ->
                "${row.dayLabel}:${row.workDate.ifBlank { "blank" }}/${row.hoursInput}"
            }

        Log.d(
            "InvoiceFinalizeDiag",
            "stage=$stage " +
                "user=${state.userId?.take(6) ?: "null"} " +
                "venue=${state.effectiveSelectedVenueId.ifBlank { "blank" }} " +
                "invoiceDate=${state.weeklyDraft.invoiceDate.ifBlank { "blank" }} " +
                "weekEnding=${state.weeklyDraft.weekEndingDate.ifBlank { "blank" }} " +
                "rate=${state.weeklyDraft.hourlyRateInput.ifBlank { "blank" }} " +
                "validInvoiceDate=${state.hasValidInvoiceDate} " +
                "validWeekEndingDate=${state.hasValidWeekEndingDate} " +
                "validHourlyRate=${state.hasValidHourlyRate} " +
                "hasWorkedShift=${state.hasWorkedShift} " +
                "canFinalize=${state.canFinalize} " +
                "workedRows=$workedRows"
        )
    }

    private fun defaultProfileDraft(cachedProfile: AuthUserModel? = null): InvoiceProfileDraft {
        val currentUser = authRepository.currentUser
        return InvoiceProfileDraft(
            fullName = cachedProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.displayName.orEmpty(),
            address = cachedProfile.toInvoiceAddress(),
            email = cachedProfile?.email?.takeIf { it.isNotBlank() }
                ?: currentUser?.email.orEmpty(),
            contactPhone = cachedProfile?.phoneNumber?.takeIf { it.isNotBlank() }
                ?: currentUser?.phoneNumber.orEmpty()
        ).normalized()
    }
}

private fun InvoiceProfileDraft.mergeMissingFrom(fallback: InvoiceProfileDraft): InvoiceProfileDraft {
    return copy(
        fullName = fullName.ifBlank { fallback.fullName },
        address = address.ifBlank { fallback.address },
        badgeNumber = badgeNumber.ifBlank { fallback.badgeNumber },
        badgeExpiryDate = badgeExpiryDate.ifBlank { fallback.badgeExpiryDate },
        utrNumber = utrNumber.ifBlank { fallback.utrNumber },
        email = email.ifBlank { fallback.email },
        contactPhone = contactPhone.ifBlank { fallback.contactPhone },
        accountNumber = accountNumber.ifBlank { fallback.accountNumber },
        sortCode = sortCode.ifBlank { fallback.sortCode },
        paymentInstructions = paymentInstructions.ifBlank { fallback.paymentInstructions },
        defaultHourlyRateInput = defaultHourlyRateInput.ifBlank { fallback.defaultHourlyRateInput },
        declaration = declaration.ifBlank { fallback.declaration }
    )
}

private data class InvoiceHydrationPayload(
    val profileAndCache: Pair<InvoiceProfileDraft?, AuthUserModel?>,
    val venues: List<InvoiceVenueDraft>,
    val weeklyDraft: InvoiceWeeklyDraft?,
    val selection: InvoiceSetupSelection
)

private fun AuthUserModel?.toInvoiceAddress(): String {
    if (this == null) return ""
    return listOf(addressLine1, addressLine2, city, postalCode, country)
        .mapNotNull { value -> value?.trim()?.takeIf { it.isNotBlank() } }
        .distinct()
        .joinToString(separator = ", ")
}

private fun InvoiceProfileDraft.isBlankForSetup(): Boolean {
    return fullName.isBlank() &&
        address.isBlank() &&
        badgeNumber.isBlank() &&
        badgeExpiryDate.isBlank() &&
        utrNumber.isBlank() &&
        email.isBlank() &&
        contactPhone.isBlank() &&
        accountNumber.isBlank() &&
        sortCode.isBlank() &&
        paymentInstructions.isBlank() &&
        defaultHourlyRateInput.isBlank()
}

private fun buildHydratedDynamicInvoice(
    profile: InvoiceProfileDraft,
    venues: List<InvoiceVenueDraft>,
    weekly: InvoiceWeeklyDraft,
    selection: InvoiceSetupSelection
): Invoice {
    val selectedVenue = venues.firstOrNull { it.venueId == weekly.selectedVenueId }
        ?: venues.firstOrNull()
    val legacyInvoice = weekly.toDynamicInvoice(profile, selectedVenue)
    val profession = selection.professionId?.let(InvoiceTemplateCatalog::profession)
    val templateId = selection.templateId
        ?: profession?.templateId
        ?: legacyInvoice.templateId
        ?: "weekly_shift_worker_template"
    return applyTemplateToInvoice(
        invoice = legacyInvoice,
        templateId = templateId,
        professionId = profession?.id ?: legacyInvoice.professionId
    )
}

private fun syncInvoiceWithVenue(
    invoice: Invoice,
    venue: InvoiceVenueDraft?
): Invoice {
    if (venue == null) return invoice
    return invoice
        .withSectionFieldValue("client_details", InvoiceFieldKeys.CLIENT_NAME, venue.venueName)
        .withSectionFieldValue("client_details", InvoiceFieldKeys.CLIENT_ADDRESS, venue.venueAddress)
}

private fun applyTemplateToInvoice(
    invoice: Invoice,
    templateId: String,
    professionId: String? = null
): Invoice {
    val lineItemCount = invoice.lineItems.size.coerceAtLeast(1)
    val baseInvoice = InvoiceFactory.createFromTemplate(
        templateId = templateId,
        invoiceId = invoice.id,
        professionId = professionId ?: invoice.professionId,
        initialLineItemCount = lineItemCount,
        currencyCode = invoice.totals.currencyCode
    )

    val sectionValueLookup = invoice.sections
        .flatMap { section -> section.fields }
        .associateBy({ it.key }, { it.value })

    val mergedSections = baseInvoice.sections.map { section ->
        section.copy(
            fields = section.fields.map { field ->
                if (sectionValueLookup.containsKey(field.key)) {
                    field.copy(value = sectionValueLookup[field.key])
                } else {
                    field
                }
            }
        )
    }

    val mergedLineItems = baseInvoice.lineItems.mapIndexed { index, lineItem ->
        val currentLineItem = invoice.lineItems.getOrNull(index)
        val currentValues = currentLineItem?.fields
            ?.associateBy({ it.key }, { it.value })
            .orEmpty()
        lineItem.copy(
            fields = lineItem.fields.map { field ->
                if (currentValues.containsKey(field.key)) {
                    field.copy(value = currentValues[field.key])
                } else {
                    field
                }
            }
        )
    }

    return InvoiceCalculations.recalculate(
        baseInvoice.copy(
            professionId = professionId ?: invoice.professionId,
            sections = mergedSections,
            lineItems = InvoiceCalculations.withComputedAmounts(mergedLineItems)
        )
    )
}

private fun Invoice.withSectionFieldValue(
    sectionId: String,
    fieldKey: String,
    value: Any?
): Invoice {
    return copy(
        sections = sections.map { section ->
            if (section.id != sectionId) {
                section
            } else {
                section.copy(
                    fields = section.fields.map { field ->
                        if (field.key == fieldKey) {
                            field.copy(value = value)
                        } else {
                            field
                        }
                    }
                )
            }
        }
    )
}

private fun Invoice.withLineItemFieldValue(
    fieldKey: String,
    transform: (InvoiceField) -> Any?
): Invoice {
    val updatedLineItems = lineItems.map { lineItem ->
        lineItem.copy(
            fields = lineItem.fields.map { field ->
                if (field.key == fieldKey) {
                    field.copy(value = transform(field))
                } else {
                    field
                }
            }
        )
    }
    return copy(lineItems = InvoiceCalculations.withComputedAmounts(updatedLineItems))
}

private fun Invoice.withLineItemFieldValueAt(
    index: Int,
    fieldKey: String,
    value: Any?
): Invoice {
    if (index !in lineItems.indices) return this
    val updatedLineItems = lineItems.mapIndexed { currentIndex, lineItem ->
        if (currentIndex != index) {
            lineItem
        } else {
            lineItem.copy(
                fields = lineItem.fields.map { field ->
                    if (field.key == fieldKey) {
                        field.copy(value = value)
                    } else {
                        field
                    }
                }
            )
        }
    }
    return copy(lineItems = InvoiceCalculations.withComputedAmounts(updatedLineItems))
}

private fun Invoice.isMeaningfullyFilled(): Boolean {
    val hasSectionValue = sections.any { section ->
        section.fields.any { field ->
            when (val value = field.value) {
                null -> false
                is String -> value.isNotBlank()
                is Number -> value.toDouble() != 0.0
                is Boolean -> value
                else -> true
            }
        }
    }
    val hasLineValue = lineItems.any { lineItem ->
        lineItem.fields.any { field ->
            when (val value = field.value) {
                null -> false
                is String -> value.isNotBlank()
                is Number -> value.toDouble() != 0.0
                is Boolean -> value
                else -> true
            }
        }
    }
    return hasSectionValue || hasLineValue
}

private fun buildNextWeeklyDraftAfterFinalize(
    venueId: String,
    fallbackHourlyRate: String
): InvoiceWeeklyDraft {
    return InvoiceWeeklyDraft(
        selectedVenueId = venueId.trim(),
        hourlyRateInput = fallbackHourlyRate.trim()
    ).withFullWeek()
}

private const val MAX_DYNAMIC_LINE_ITEMS = 7

private val PROGRESSIVE_FORM_STEPS = listOf(
    InvoiceFormStep.INVOICE_INFO,
    InvoiceFormStep.WORKER_DETAILS,
    InvoiceFormStep.CLIENT_DETAILS,
    InvoiceFormStep.WORK_DETAILS,
    InvoiceFormStep.REVIEW
)

private fun InvoiceFormStep.nextProgressiveStep(): InvoiceFormStep? {
    if (this == InvoiceFormStep.QUICK_START) return InvoiceFormStep.INVOICE_INFO
    val index = PROGRESSIVE_FORM_STEPS.indexOf(this)
    if (index < 0 || index == PROGRESSIVE_FORM_STEPS.lastIndex) return null
    return PROGRESSIVE_FORM_STEPS[index + 1]
}

private fun InvoiceFormStep.previousProgressiveStep(): InvoiceFormStep? {
    if (this == InvoiceFormStep.QUICK_START) return null
    if (this == InvoiceFormStep.INVOICE_INFO) return InvoiceFormStep.QUICK_START
    val index = PROGRESSIVE_FORM_STEPS.indexOf(this)
    if (index <= 0) return null
    return PROGRESSIVE_FORM_STEPS[index - 1]
}

private fun InvoiceSetupUiState.validationErrorForCurrentStep(): String? {
    return when (formStep) {
        InvoiceFormStep.QUICK_START -> {
            if (selectedProfession != null || selectedTemplate != null) {
                null
            } else {
                "Choose the type of work that best matches this invoice"
            }
        }

        InvoiceFormStep.INVOICE_INFO -> {
            if (hasRequiredSectionFields("invoice_info")) {
                null
            } else {
                "Complete the required invoice details before continuing"
            }
        }

        InvoiceFormStep.WORKER_DETAILS -> {
            if (hasRequiredSectionFields("worker_details")) {
                null
            } else {
                "Complete the required worker details before continuing"
            }
        }

        InvoiceFormStep.CLIENT_DETAILS -> when {
            effectiveSelectedVenueId.isBlank() -> "Select or add a client / venue before continuing"
            !hasRequiredSectionFields(
                sectionId = "client_details",
                ignoredKeys = setOf(
                    InvoiceFieldKeys.CLIENT_NAME,
                    InvoiceFieldKeys.CLIENT_ADDRESS
                )
            ) -> "Complete the remaining client details before continuing"
            else -> null
        }

        InvoiceFormStep.WORK_DETAILS -> when {
            !hasRequiredWorkSections() -> "Complete the remaining required work details before continuing"
            !hasReadyLineItem() -> "Add at least one completed line item before continuing"
            else -> null
        }

        InvoiceFormStep.REVIEW -> {
            if (canFinalize) {
                null
            } else {
                "Complete the invoice dates, rate, and at least one worked shift before finalizing"
            }
        }
    }
}

private fun InvoiceSetupUiState.hasRequiredSectionFields(
    sectionId: String,
    ignoredKeys: Set<String> = emptySet()
): Boolean {
    val section = draftInvoice.sections.firstOrNull { it.id == sectionId } ?: return true
    return section.fields
        .filter { field -> field.required && field.key !in ignoredKeys }
        .all(InvoiceField::isCompleted)
}

private fun InvoiceSetupUiState.hasRequiredWorkSections(): Boolean {
    val workSections = draftInvoice.sections.filterNot { section ->
        section.id in setOf("invoice_info", "worker_details", "client_details")
    }
    return workSections.all { section ->
        section.fields
            .filter { field -> field.required }
            .all(InvoiceField::isCompleted)
    }
}

private fun InvoiceSetupUiState.hasReadyLineItem(): Boolean {
    return draftInvoice.lineItems.any { lineItem ->
        val requiredFieldsCompleted = lineItem.fields
            .filter { field ->
                field.required && field.key != InvoiceFieldKeys.LINE_AMOUNT
            }
            .all(InvoiceField::isCompleted)
        val workedHours = lineItem.fields
            .firstOrNull { field -> field.key == InvoiceFieldKeys.LINE_HOURS }
            ?.doubleValue()
            ?: 0.0
        requiredFieldsCompleted && workedHours > 0.0
    }
}

private fun InvoiceField.isCompleted(): Boolean {
    return when (val raw = value) {
        null -> false
        is String -> raw.trim().isNotBlank()
        else -> true
    }
}

private fun InvoiceField.coerceInputValue(rawValue: Any?): Any? {
    return when (type) {
        FieldType.BOOLEAN -> when (rawValue) {
            is Boolean -> rawValue
            is String -> rawValue.toBooleanStrictOrNull() ?: false
            else -> false
        }

        FieldType.NUMBER,
        FieldType.CURRENCY,
        FieldType.DURATION -> rawValue?.toString()?.let(::sanitizeDecimal).orEmpty()

        FieldType.TEXT,
        FieldType.DATE,
        FieldType.TIME,
        FieldType.DROPDOWN -> rawValue?.toString()?.trim().orEmpty()
    }
}

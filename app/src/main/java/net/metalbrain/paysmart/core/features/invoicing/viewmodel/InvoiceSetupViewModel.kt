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
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceReadRepository
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceVenueRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceProfileDraft
import net.metalbrain.paysmart.core.features.invoicing.data.InvoiceWeeklyDraftRepository
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceVenueDraft
import net.metalbrain.paysmart.core.features.invoicing.domain.InvoiceWeeklyDraft
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.model.AuthUserModel
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

    fun updateProfileFullName(value: String) {
        updateProfileDraft { it.copy(fullName = value) }
    }

    fun updateProfileAddress(value: String) {
        updateProfileDraft { it.copy(address = value) }
    }

    fun updateProfileBadgeNumber(value: String) {
        updateProfileDraft { it.copy(badgeNumber = value) }
    }

    fun updateProfileBadgeExpiryDate(value: String) {
        updateProfileDraft { it.copy(badgeExpiryDate = value) }
    }

    fun updateProfileUtrNumber(value: String) {
        updateProfileDraft { it.copy(utrNumber = value) }
    }

    fun updateProfileEmail(value: String) {
        updateProfileDraft { it.copy(email = value) }
    }

    fun updateProfileContactPhone(value: String) {
        updateProfileDraft { it.copy(contactPhone = value) }
    }

    fun updateProfileAccountNumber(value: String) {
        updateProfileDraft { it.copy(accountNumber = value) }
    }

    fun updateProfileSortCode(value: String) {
        updateProfileDraft { it.copy(sortCode = value) }
    }

    fun updateProfilePaymentInstructions(value: String) {
        updateProfileDraft { it.copy(paymentInstructions = value) }
    }

    fun updateProfileDefaultHourlyRate(value: String) {
        updateProfileDraft { it.copy(defaultHourlyRateInput = sanitizeDecimal(value)) }
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
                            infoMessage = "Invoice profile saved"
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
                            infoMessage = "Invoice profile saved"
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
        updateWeeklyDraft { it.copy(selectedVenueId = venueId.trim()) }
    }

    fun updateInvoiceDate(value: String) {
        updateWeeklyDraft { it.copy(invoiceDate = value.trim()) }
    }

    fun updateWeekEndingDate(value: String) {
        updateWeeklyDraft { it.copy(weekEndingDate = value.trim()) }
    }

    fun updateHourlyRateInput(value: String) {
        updateWeeklyDraft { it.copy(hourlyRateInput = sanitizeDecimal(value)) }
    }

    fun updateShiftDate(dayLabel: String, value: String) {
        updateWeeklyDraft { weekly ->
            weekly.copy(
                shifts = weekly.withFullWeek().shifts.map { shift ->
                    if (sameDay(shift.dayLabel, dayLabel)) {
                        shift.copy(workDate = value.trim())
                    } else {
                        shift
                    }
                }
            )
        }
    }

    fun updateShiftDateAt(index: Int, value: String) {
        val dayLabel = _uiState.value.weeklyRows.getOrNull(index)?.dayLabel ?: return
        updateShiftDate(dayLabel, value)
    }

    fun updateShiftHours(dayLabel: String, value: String) {
        val sanitized = sanitizeDecimal(value)
        updateWeeklyDraft { weekly ->
            weekly.copy(
                shifts = weekly.withFullWeek().shifts.map { shift ->
                    if (sameDay(shift.dayLabel, dayLabel)) {
                        shift.copy(hoursInput = sanitized)
                    } else {
                        shift
                    }
                }
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
                        it.copy(
                            isFinalizing = false,
                            weeklyDraft = nextDraft,
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
                weeklyDraftRepository.observeByUserId(userId)
            ) { profileAndCache, venues, weekly ->
                Triple(profileAndCache, venues, weekly)
            }.collectLatest { (profileAndCache, venues, weekly) ->
                val (profile, cachedProfile) = profileAndCache
                val fallbackProfile = (profile ?: InvoiceProfileDraft())
                    .mergeMissingFrom(defaultProfileDraft(cachedProfile))
                _uiState.update { current ->
                    val hydratedWeekly = (weekly ?: InvoiceWeeklyDraft()).withFullWeek()
                    val selectedVenueId = hydratedWeekly.selectedVenueId
                        .takeIf { id -> venues.any { it.venueId == id } }
                        ?: venues.firstOrNull()?.venueId.orEmpty()
                    current.copy(
                        profileDraft = if (current.profileDraft.isBlankForSetup()) {
                            fallbackProfile
                        } else {
                            current.profileDraft.mergeMissingFrom(fallbackProfile)
                        },
                        venues = venues,
                        weeklyDraft = hydratedWeekly.copy(selectedVenueId = selectedVenueId),
                        isHydrating = false,
                        isPersisting = false
                    )
                }
            }
        }
    }

    private fun updateWeeklyDraft(transform: (InvoiceWeeklyDraft) -> InvoiceWeeklyDraft) {
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Authentication required before invoice setup") }
            return
        }

        val next = transform(_uiState.value.weeklyDraft).normalized().withFullWeek()
        _uiState.update {
            it.copy(
                weeklyDraft = next,
                isPersisting = true,
                error = null,
                infoMessage = null,
                finalizedInvoice = null
            )
        }
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

    private fun updateProfileDraft(transform: (InvoiceProfileDraft) -> InvoiceProfileDraft) {
        _uiState.update {
            it.copy(
                profileDraft = transform(it.profileDraft),
                error = null,
                infoMessage = null
            )
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

private fun buildNextWeeklyDraftAfterFinalize(
    venueId: String,
    fallbackHourlyRate: String
): InvoiceWeeklyDraft {
    return InvoiceWeeklyDraft(
        selectedVenueId = venueId.trim(),
        hourlyRateInput = fallbackHourlyRate.trim()
    ).withFullWeek()
}

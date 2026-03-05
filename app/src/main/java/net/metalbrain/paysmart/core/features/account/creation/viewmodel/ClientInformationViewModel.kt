package net.metalbrain.paysmart.core.features.account.creation.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.profile.data.repository.ProfileRepository
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class ClientInformationUiState(
    val countryIso2: String = "GB",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val canContinue: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && dateOfBirth.isNotBlank()
}

@HiltViewModel
class ClientInformationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileCacheRepository: UserProfileCacheRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientInformationUiState())
    val uiState: StateFlow<ClientInformationUiState> = _uiState.asStateFlow()

    fun bindCountry(iso2: String) {
        val normalized = normalizeCountryIso2(iso2, DEFAULT_COUNTRY_ISO2)
        _uiState.update { it.copy(countryIso2 = normalized) }
        hydrateFromCachedProfile()
    }

    fun onFirstNameChanged(value: String) = updateField { copy(firstName = value, error = null) }
    fun onMiddleNameChanged(value: String) = updateField { copy(middleName = value, error = null) }
    fun onLastNameChanged(value: String) = updateField { copy(lastName = value, error = null) }
    fun onEmailChanged(value: String) = updateField { copy(email = value, error = null) }
    fun onDateOfBirthChanged(value: String) = updateField { copy(dateOfBirth = value, error = null) }
    fun clearError() = updateField { copy(error = null) }

    fun submit(onSuccess: () -> Unit) {
        val current = _uiState.value
        val validationError = validate(current)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val fullName = listOf(
                current.firstName.trim(),
                current.middleName.trim().ifBlank { null },
                current.lastName.trim()
            ).filterNotNull().joinToString(" ")

            val result = profileRepository.saveProfileDraft(
                ProfileDetailsDraft(
                    fullName = fullName,
                    email = current.email.trim(),
                    dateOfBirth = current.dateOfBirth.trim(),
                    country = current.countryIso2
                )
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false, error = null) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Unable to save client information"
                    )
                }
            }
        }
    }

    private fun hydrateFromCachedProfile() {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val cached = userProfileCacheRepository.observeByUid(uid).firstOrNull() ?: return@launch
            val nameParts = splitDisplayName(cached.displayName.orEmpty())
            _uiState.update { current ->
                current.copy(
                    firstName = current.firstName.ifBlank { nameParts.firstName },
                    middleName = current.middleName.ifBlank { nameParts.middleName ?: "" },
                    lastName = current.lastName.ifBlank { nameParts.lastName },
                    email = current.email.ifBlank { cached.email.orEmpty() },
                    dateOfBirth = current.dateOfBirth.ifBlank { cached.dateOfBirth.orEmpty() }
                )
            }
        }
    }

    private fun validate(state: ClientInformationUiState): String? {
        if (!state.canContinue) return "Complete all required client information fields"
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            return "Enter a valid email address"
        }
        val dob = try {
            LocalDate.parse(state.dateOfBirth.trim())
        } catch (_: DateTimeParseException) {
            return "Date of birth must use YYYY-MM-DD format"
        }
        val now = LocalDate.now()
        if (dob.isAfter(now)) {
            return "Date of birth cannot be in the future"
        }
        if (dob.isBefore(now.minusYears(120))) {
            return "Enter a valid date of birth"
        }
        if (dob.plusYears(18).isAfter(now)) {
            return "You must be 18 or older to continue"
        }
        return null
    }

    private fun updateField(transform: ClientInformationUiState.() -> ClientInformationUiState) {
        _uiState.update(transform)
    }
}

private data class DisplayNameParts(val firstName: String, val middleName: String?, val lastName: String)

private fun splitDisplayName(rawName: String): DisplayNameParts {
    val parts = rawName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> DisplayNameParts("", null, "")
        parts.size == 1 -> DisplayNameParts(parts.first(), null, "")
        parts.size == 2 -> DisplayNameParts(parts.first(), null, parts.last())
        else -> DisplayNameParts(
            firstName = parts.first(),
            middleName = parts.subList(1, parts.lastIndex).joinToString(" ").ifBlank { null },
            lastName = parts.last()
        )
    }
}

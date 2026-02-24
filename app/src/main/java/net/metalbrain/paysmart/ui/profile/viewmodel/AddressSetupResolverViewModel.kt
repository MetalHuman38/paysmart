package net.metalbrain.paysmart.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.AddressLookupPayload
import net.metalbrain.paysmart.core.auth.AddressLookupResult
import net.metalbrain.paysmart.core.auth.AddressResolverPolicyHandler
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
import net.metalbrain.paysmart.ui.profile.data.repository.ProfileRepository

enum class AddressSetupResolverStep {
    INPUT,
    MAP_CONFIRM,
    FINAL_CONFIRM
}

data class AddressSetupResolverUiState(
    val house: String = "",
    val postcode: String = "",
    val country: String = "GB",
    val step: AddressSetupResolverStep = AddressSetupResolverStep.INPUT,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val resolvedAddress: AddressLookupResult? = null,
    val line1Draft: String = "",
    val line2Draft: String = "",
    val cityDraft: String = "",
    val stateOrRegionDraft: String = "",
    val postCodeDraft: String = "",
    val countryCodeDraft: String = "",
    val error: String? = null
)

@HiltViewModel
class AddressSetupResolverViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val resolverPolicyHandler: AddressResolverPolicyHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressSetupResolverUiState())
    val uiState: StateFlow<AddressSetupResolverUiState> = _uiState.asStateFlow()

    fun onHouseChanged(value: String) {
        _uiState.update { it.copy(house = value, error = null) }
    }

    fun onPostcodeChanged(value: String) {
        _uiState.update { it.copy(postcode = value, error = null) }
    }

    fun onCountryChanged(value: String) {
        _uiState.update { it.copy(country = value, error = null) }
    }

    fun onLine1DraftChanged(value: String) {
        _uiState.update { it.copy(line1Draft = value, error = null) }
    }

    fun onLine2DraftChanged(value: String) {
        _uiState.update { it.copy(line2Draft = value, error = null) }
    }

    fun onCityDraftChanged(value: String) {
        _uiState.update { it.copy(cityDraft = value, error = null) }
    }

    fun onStateOrRegionDraftChanged(value: String) {
        _uiState.update { it.copy(stateOrRegionDraft = value, error = null) }
    }

    fun onPostCodeDraftChanged(value: String) {
        _uiState.update { it.copy(postCodeDraft = value, error = null) }
    }

    fun onCountryCodeDraftChanged(value: String) {
        _uiState.update { it.copy(countryCodeDraft = value, error = null) }
    }

    fun goToFinalConfirmation() {
        val resolved = _uiState.value.resolvedAddress ?: return
        _uiState.update { state ->
            state.copy(
                step = AddressSetupResolverStep.FINAL_CONFIRM,
                line1Draft = state.line1Draft.ifBlank { resolved.line1.ifBlank { resolved.fullAddressWithHouse } },
                line2Draft = state.line2Draft.ifBlank { resolved.line2.orEmpty() },
                cityDraft = state.cityDraft.ifBlank { resolved.city.orEmpty() },
                stateOrRegionDraft = state.stateOrRegionDraft.ifBlank { resolved.stateOrRegion.orEmpty() },
                postCodeDraft = state.postCodeDraft.ifBlank { resolved.postCode },
                countryCodeDraft = state.countryCodeDraft.ifBlank { resolved.countryCode },
                error = null
            )
        }
    }

    fun backToInput() {
        _uiState.update {
            it.copy(
                step = AddressSetupResolverStep.INPUT,
                resolvedAddress = null,
                error = null
            )
        }
    }

    fun backToMapConfirm() {
        _uiState.update {
            if (it.resolvedAddress == null) {
                it.copy(step = AddressSetupResolverStep.INPUT, error = null)
            } else {
                it.copy(step = AddressSetupResolverStep.MAP_CONFIRM, error = null)
            }
        }
    }

    fun resolveAddress() {
        val currentState = _uiState.value
        if (currentState.postcode.isBlank()) {
            _uiState.update { it.copy(error = "Postcode is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, resolvedAddress = null) }

            val session = runCatching { authRepository.getCurrentSessionOrThrow() }
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.localizedMessage ?: "Authentication required"
                        )
                    }
                    return@launch
                }

            val payload = AddressLookupPayload(
                house = currentState.house,
                postcode = currentState.postcode,
                country = currentState.country
            )

            val lookup = resolverPolicyHandler.resolveAddress(session.idToken, payload)
            lookup
                .onSuccess { resolved ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = AddressSetupResolverStep.MAP_CONFIRM,
                            resolvedAddress = resolved,
                            line1Draft = resolved.line1.ifBlank { resolved.fullAddressWithHouse },
                            line2Draft = resolved.line2.orEmpty(),
                            cityDraft = resolved.city.orEmpty(),
                            stateOrRegionDraft = resolved.stateOrRegion.orEmpty(),
                            postCodeDraft = resolved.postCode,
                            countryCodeDraft = resolved.countryCode,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.localizedMessage ?: "Unable to resolve address"
                        )
                    }
                }
        }
    }

    fun applyResolvedAddress(onSaved: () -> Unit) {
        val currentState = _uiState.value
        val resolved = currentState.resolvedAddress ?: return
        val line1 = currentState.line1Draft.trim().ifBlank { resolved.line1.ifBlank { resolved.fullAddressWithHouse } }
        val postCode = currentState.postCodeDraft.trim().ifBlank { resolved.postCode }
        val countryCode = currentState.countryCodeDraft.trim().ifBlank { resolved.countryCode }

        if (line1.isBlank() || postCode.isBlank() || countryCode.isBlank()) {
            _uiState.update {
                it.copy(error = "Address line 1, postcode, and country code are required")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val saveResult = profileRepository.saveProfileDraft(
                ProfileDetailsDraft(
                    addressLine1 = line1,
                    addressLine2 = currentState.line2Draft.trim().ifBlank { null },
                    city = currentState.cityDraft.trim().ifBlank { null },
                    country = countryCode.uppercase(),
                    postalCode = postCode.uppercase()
                )
            )

            if (saveResult.isSuccess) {
                val verificationResult = profileRepository.markHomeAddressVerified()
                if (verificationResult.isSuccess) {
                    _uiState.update { it.copy(isSaving = false, error = null) }
                    onSaved()
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = verificationResult.exceptionOrNull()?.localizedMessage
                                ?: "Unable to mark address as verified"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = saveResult.exceptionOrNull()?.localizedMessage
                            ?: "Unable to save address"
                    )
                }
            }
        }
    }
}

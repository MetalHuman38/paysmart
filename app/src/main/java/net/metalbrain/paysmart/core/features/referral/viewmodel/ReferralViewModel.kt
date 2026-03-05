package net.metalbrain.paysmart.core.features.referral.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.metalbrain.paysmart.core.features.referral.model.ReferralUiState

@HiltViewModel
class ReferralViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    fun onReferralCodeChanged(value: String) {
        val normalized = value
            .uppercase()
            .filter { it.isLetterOrDigit() || it == '-' }
            .take(32)
        _uiState.update { current ->
            current.copy(enteredCode = normalized)
        }
    }

    fun submitReferralCode() {
        if (!_uiState.value.canSubmit) return
        _uiState.update { current ->
            current.copy(isSubmitting = true)
        }
        // Placeholder for backend submission.
        _uiState.update { current ->
            current.copy(isSubmitting = false)
        }
    }
}

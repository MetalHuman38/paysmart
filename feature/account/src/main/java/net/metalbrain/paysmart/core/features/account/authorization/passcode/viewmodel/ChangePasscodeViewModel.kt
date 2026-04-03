package net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.core.features.account.authorization.passcode.repository.PasscodeRepository

@HiltViewModel
class ChangePasscodeViewModel @Inject constructor(
    private val passcodeRepository: PasscodeRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    data class UiState(
        val currentPasscode: String = "",
        val newPasscode: String = "",
        val confirmPasscode: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onCurrentPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(currentPasscode = value, error = null)
    }

    fun onNewPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(newPasscode = value, error = null)
    }

    fun onConfirmPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPasscode = value, error = null)
    }

    fun submitChange(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val current = state.currentPasscode
            val next = state.newPasscode
            val confirm = state.confirmPasscode

            if (current.isBlank() || next.length !in 4..6 || next != confirm) {
                _uiState.value = state.copy(
                    loading = false,
                    error = context.getString(R.string.change_passcode_validation_error)
                )
                return@launch
            }

            if (current == next) {
                _uiState.value = state.copy(
                    loading = false,
                    error = context.getString(R.string.change_passcode_reuse_error)
                )
                return@launch
            }

            _uiState.value = state.copy(loading = true, error = null)

            try {
                val changed = passcodeRepository.changePasscode(
                    currentPasscode = current,
                    newPasscode = next
                )
                if (!changed) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = context.getString(R.string.change_passcode_incorrect_current_error)
                    )
                    return@launch
                }

                _uiState.value = UiState()
                onSuccess()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = context.getString(R.string.change_passcode_update_error)
                )
            }
        }
    }
}

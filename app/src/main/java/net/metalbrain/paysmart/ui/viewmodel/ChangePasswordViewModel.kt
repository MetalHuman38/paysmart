package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.PasswordRepository

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val passwordRepo: PasswordRepository
) : ViewModel() {

    data class UiState(
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmNewPassword: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onOldPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(oldPassword = value)
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value)
    }

    fun onConfirmNewPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmNewPassword = value)
    }

    fun submitPasswordChange() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            try {
                val state = _uiState.value
                if (state.newPassword != state.confirmNewPassword) {
                    _uiState.value = state.copy(error = "Passwords do not match", loading = false)
                    return@launch
                }

                val changed = passwordRepo.changePassword(
                    old = state.oldPassword,
                    new = state.newPassword
                )

                if (!changed) {
                    _uiState.value = state.copy(error = "Incorrect current password", loading = false)
                    return@launch
                }

                _uiState.value = state.copy(success = true, loading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Something went wrong: ${e.message}",
                    loading = false
                )
            }
        }
    }
}

package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.PasswordRepository
import javax.inject.Inject

@HiltViewModel
class EnterPasswordViewModel @Inject constructor(
    private val passwordRepo: PasswordRepository
) : ViewModel() {

    data class UiState(
        val password: String = "",
        val errorMessage: String? = null,
        val loading: Boolean = false,
        val success: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val password = _uiState.value.password
                val isValid = passwordRepo.verify(password)

                if (isValid) {
                    _uiState.value = _uiState.value.copy(success = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Incorrect password.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "An error occurred. Please try again."
                )
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

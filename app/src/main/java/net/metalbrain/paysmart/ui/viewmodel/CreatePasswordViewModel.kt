package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.PasswordRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import javax.inject.Inject

@HiltViewModel
class CreatePasswordViewModel @Inject constructor(
    private val passwordRepo: PasswordRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    data class UiState(
        val password: String = "",
        val confirmPassword: String = "",
        val showPassword: Boolean = false,
        val loading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun submitPassword(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val password = _uiState.value.password

                // Set secure password (EncryptedFile + hashing)
                passwordRepo.setPassword(password)

                // Update Firestore flag ✅
                val uid = getCurrentUserUid()
                if (uid != null) {
                    userProfileRepository.updateProgressFlags(
                        uid = uid,
                        progressFlags = mapOf(
                            "hasLocalPassword" to true
                        )
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("CreatePasswordViewModel", "Failed to save password", e)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    private fun getCurrentUserUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}

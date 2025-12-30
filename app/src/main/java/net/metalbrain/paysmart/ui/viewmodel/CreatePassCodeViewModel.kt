package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import javax.inject.Inject

@HiltViewModel
class PasscodeViewModel @Inject constructor(
    private val passcodeRepo: PasscodeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    data class UiState(
        val passcode: String = "",
        val confirmPasscode: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(passcode = value, error = null)
    }

    fun onConfirmPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPasscode = value, error = null)
    }

    fun submitPasscode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val code = _uiState.value.passcode
            val confirm = _uiState.value.confirmPasscode

            if (code.length < 4 || code != confirm) {
                _uiState.value = _uiState.value.copy(error = "Passcodes do not match or too short")
                return@launch
            }

            _uiState.value = _uiState.value.copy(loading = true)

            try {
                passcodeRepo.setPasscode(code)

                // 🔐 Update Firestore flag
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    userProfileRepository.touchLastSignedIn(
                        uid = uid,
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("PasscodeViewModel", "Error setting passcode", e)
                _uiState.value = _uiState.value.copy(error = "Failed to save passcode")
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

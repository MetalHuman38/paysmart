package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.PassCodePolicyHandler
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import javax.inject.Inject

@HiltViewModel
class PasscodeViewModel @Inject constructor(
    private val passcodeRepo: PasscodeRepository,
    private val userProfileRepository: UserProfileRepository,
    private val passcodePolicyHandler: PassCodePolicyHandler,
) : ViewModel() {

    data class UiState(
        val passcode: String = "",
        val confirmPasscode: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _passcodeSet = MutableStateFlow(false)
    val passcodeSet: StateFlow<Boolean> = _passcodeSet

    fun onPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(passcode = value, error = null)
    }

    fun onConfirmPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPasscode = value, error = null)
    }

    fun submitPasscode(onSuccess: () -> Unit) {
        Log.d("PasscodeViewModel", "submitPasscode() launched")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val code = _uiState.value.passcode
            val confirm = _uiState.value.confirmPasscode
            if (code.length < 4 || code != confirm) {
                _uiState.value = _uiState.value.copy(error = "Passcodes do not match or too short")
                return@launch
            }

            _uiState.value = _uiState.value.copy(loading = true)

            try {

                // 🔐 Check if user is logged in
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.w("CreatePassCodeViewModel", "User not logged in")
                    throw IllegalStateException("User not authenticated")
                }

                passcodeRepo.setPasscode(code)

                // ✅ 3. Notify server to mark passwordEnabled = true
                val idToken = user.getIdToken(false).await().token
                if (idToken == null) {
                    Log.w("CreatePasswordViewModel", "No ID token available")
                    throw IllegalStateException("No ID token available")
                }

                // Check if user has passcode
                val hasPasscode = passcodePolicyHandler.getPasswordEnabled(idToken)
                if (hasPasscode) {
                    Log.w("CreatePasswordViewModel", "Passcode already enabled")
                    throw IllegalStateException("Passcode already enabled")
                } else {
                    val success = passcodePolicyHandler.setPassCodeEnabled(idToken)
                    if (!success) {
                        Log.w("CreatePasswordViewModel", "Server did not confirm passwordEnabled flag")

                    }
                }
                _passcodeSet.value = true

                // ✅ 4. Update user profile (optional)
                userProfileRepository.touchLastSignedIn(uid = user.uid)

                onSuccess()

            } catch (e: Exception) {
                Log.e("PasscodeViewModel", "Error setting passcode", e)
                _uiState.value = _uiState.value.copy(error = "Failed to save passcode")
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun verifyPasscode(passcode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (passcodeRepo.verify(passcode)) {
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Invalid passcode")
                }
            } catch (e: Exception) {
                Log.e("PasscodeViewModel", "Error verifying passcode", e)
                _uiState.value = _uiState.value.copy(error = "Failed to verify passcode")
            }
        }
    }

    fun resetPasscodeSet() {
        _passcodeSet.value = false
    }
}

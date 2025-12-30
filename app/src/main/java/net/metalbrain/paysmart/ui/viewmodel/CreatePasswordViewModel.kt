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
import net.metalbrain.paysmart.core.auth.PasswordPolicyHandler
import net.metalbrain.paysmart.data.repository.PasswordRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import javax.inject.Inject

@HiltViewModel
class CreatePasswordViewModel @Inject constructor(
    private val passwordRepo: PasswordRepository,
    private val userProfileRepository: UserProfileRepository,
    private val passwordPolicyHandler: PasswordPolicyHandler
) : ViewModel() {

    data class UiState(
        val password: String = "",
        val confirmPassword: String = "",
        val showPassword: Boolean = false,
        val loading: Boolean = false,
        val errorMessage: String? = null
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

                // ✅ 1. Validate required state
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.w("CreatePasswordViewModel", "User not logged in")
                    throw IllegalStateException("User not authenticated")
                }

                // ✅ 2. Store password securely (locally)
                passwordRepo.setPassword(password)

                // ✅ 3. Notify server to mark passwordEnabled = true
                val idToken = user.getIdToken(false).await().token
                val serverUpdated = passwordPolicyHandler.setPasswordEnabled(idToken ?: "")

                if (!serverUpdated) {
                    Log.w("CreatePasswordViewModel", "Server did not confirm passwordEnabled flag")
                    // Optionally retry later or show a warning
                }

                // ✅ 4. Update user profile (optional)
                userProfileRepository.touchLastSignedIn(uid = user.uid)

                onSuccess()
            } catch (e: Exception) {
                Log.e("CreatePasswordViewModel", "Failed to save password", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save password. Please try again."
                )
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

}

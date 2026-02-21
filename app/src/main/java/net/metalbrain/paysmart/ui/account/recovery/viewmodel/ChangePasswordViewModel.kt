package net.metalbrain.paysmart.ui.account.recovery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

                // --- Validation ---
                when {
                    state.newPassword != state.confirmNewPassword -> {
                        _uiState.value = state.copy(error = "Passwords do not match")
                        return@launch
                    }

                    state.newPassword == state.oldPassword -> {
                        _uiState.value =
                            state.copy(error = "New password cannot be the same as the old password")
                        return@launch
                    }
                    // It's a good practice to enforce password strength
                    state.newPassword.length < 8 -> {
                        _uiState.value =
                            state.copy(error = "Password must be at least 8 characters long")
                        return@launch
                    }
                }

                // --- Authentication and Repository Call ---
                val user = FirebaseAuth.getInstance().currentUser
                    ?: throw IllegalStateException("User not authenticated")
                val idToken = user.getIdToken(false).await().token
                    ?: throw IllegalStateException("Token missing")

                val changed = passwordRepo.changePassword(
                    old = state.oldPassword,
                    new = state.newPassword,
                    idToken = idToken
                )

                if (changed) {
                    _uiState.value = state.copy(success = true)
                } else {
                    _uiState.value = state.copy(error = "Incorrect current password")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Something went wrong: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

//    fun submitPasswordChange() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(loading = true, error = null)
//
//            try {
//                val state = _uiState.value
//                if (state.newPassword != state.confirmNewPassword) {
//                    _uiState.value = state.copy(error = "Passwords do not match", loading = false)
//                    return@launch
//                }
//
//                // If new password is the same as the old password, don't change it.
//                if (state.newPassword == state.oldPassword) {
//                    _uiState.value = state.copy(error = "New password cannot be the same as the old password", loading = false)
//                    return@launch
//                }
//
//                val user = FirebaseAuth.getInstance().currentUser
//                    ?: throw IllegalStateException("User not authenticated")
//                val idToken = user.getIdToken(false).await().token
//                    ?: throw IllegalStateException("Token missing")
//
//                val changed = passwordRepo.changePassword(
//                    old = state.oldPassword,
//                    new = state.newPassword,
//                    idToken = idToken
//                )
//
//                if (!changed) {
//                    _uiState.value = state.copy(error = "Incorrect current password", loading = false)
//                    return@launch
//                }
//
//                _uiState.value = state.copy(success = true, loading = false)
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    error = "Something went wrong: ${e.message}",
//                    loading = false
//                )
//            }
//        }
//    }
//}

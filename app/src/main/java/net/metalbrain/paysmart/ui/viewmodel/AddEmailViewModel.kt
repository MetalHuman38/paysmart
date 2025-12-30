package net.metalbrain.paysmart.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.EmailVerificationHandler
import javax.inject.Inject

@HiltViewModel
class AddEmailViewModel @Inject constructor(
    private val emailVerificationHandler: EmailVerificationHandler
) : ViewModel() {

    data class EmailUiState(
        val email: String = "",
        val emailValid: Boolean = false,
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailValid = Patterns.EMAIL_ADDRESS.matcher(value).matches(),
            error = null
        )
    }

    fun sendVerificationEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.emailValid || state.loading) return

        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)

            try {
                val user = FirebaseAuth.getInstance().currentUser
                    ?: error("User not logged in")

                val token = user.getIdToken(false).await().token
                    ?: error("Failed to get ID token")

                val ok = emailVerificationHandler.sendVerification(
                    idToken = token,
                    email = state.email
                )

                if (!ok) error("Failed to send verification email")

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to send email"
                )
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}

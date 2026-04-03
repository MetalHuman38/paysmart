package net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraft
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraftStore
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailVerificationNotificationGateway
import net.metalbrain.paysmart.core.features.account.authentication.email.remote.EmailVerificationHandler

@HiltViewModel
class EmailSentViewModel @Inject constructor(
    private val emailVerificationHandler: EmailVerificationHandler,
    private val emailDraftStore: EmailDraftStore,
    private val firebaseAuth: FirebaseAuth,
    private val emailVerificationNotificationGateway: EmailVerificationNotificationGateway,
) : ViewModel() {

    data class EmailSentUiState(
        val isResending: Boolean = false,
        val infoMessage: String? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(EmailSentUiState())
    val uiState: StateFlow<EmailSentUiState> = _uiState
    private var verificationCheckInFlight = false

    fun resendVerificationEmail(email: String, returnRoute: String) {
        if (email.isBlank() || _uiState.value.isResending) return

        viewModelScope.launch {
            _uiState.value = EmailSentUiState(isResending = true)
            try {
                val user = firebaseAuth.currentUser
                    ?: error("User not logged in")

                val token = user.getIdToken(false).await().token
                    ?: error("Failed to get ID token")

                val ok = emailVerificationHandler.sendVerification(
                    idToken = token,
                    email = email,
                    returnRoute = returnRoute
                )
                if (!ok) error("Failed to resend verification email")

                emailDraftStore.saveDraft(EmailDraft(email = email, verified = false))

                _uiState.value = EmailSentUiState(
                    isResending = false,
                    infoMessage = "Verification email sent again."
                )
            } catch (error: Exception) {
                _uiState.value = EmailSentUiState(
                    isResending = false,
                    errorMessage = error.message ?: "Unable to resend verification email"
                )
            }
        }
    }

    fun consumeTransientMessage() {
        _uiState.value = _uiState.value.copy(
            infoMessage = null,
            errorMessage = null
        )
    }

    fun refreshVerificationStatus(
        email: String,
        onVerified: () -> Unit,
    ) {
        if (verificationCheckInFlight) {
            return
        }

        viewModelScope.launch {
            verificationCheckInFlight = true
            try {
                val user = firebaseAuth.currentUser ?: return@launch
                user.reload().await()

                val resolvedEmail = user.email?.trim().takeUnless { it.isNullOrBlank() }
                    ?: email.trim()
                val token = user.getIdToken(true).await().token.orEmpty()
                val remoteVerified = if (token.isNotBlank() && resolvedEmail.isNotBlank()) {
                    emailVerificationHandler.isVerified(token, resolvedEmail)
                } else {
                    false
                }

                if (user.isEmailVerified || remoteVerified) {
                    emailDraftStore.saveDraft(
                        EmailDraft(
                            email = resolvedEmail.ifBlank { email },
                            verified = true
                        )
                    )
                    emailVerificationNotificationGateway.syncEmailVerifiedNotification(
                        uid = user.uid,
                        email = resolvedEmail.ifBlank { email }
                    )
                    onVerified()
                }
            } catch (error: Exception) {
                Log.w("EmailSentViewModel", "Verification refresh failed", error)
            } finally {
                verificationCheckInFlight = false
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.account.creation.phone.viewModel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraft
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraftStore
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier,
    private val auth: FirebaseAuth,
    private val authPolicyHandler: AuthPolicyHandler,
    private val phoneDraftStore: PhoneDraftStore
) : ViewModel() {

    private companion object {
        private const val USER_FINALIZE_TIMEOUT_MS = 10_000L
    }

    data class UiState(
        val loading: Boolean = false,
        val verificationReady: Boolean = false,
        val awaitingCode: Boolean = true,
        val verified: Boolean = false,
        val sessionErrorMessage: String? = null,
        val actionErrorMessage: String? = null,
        val remainingSeconds: Int = 0,
        val isResendAvailable: Boolean = false,
        val isResending: Boolean = false
    ) {
        val displayErrorMessage: String?
            get() = actionErrorMessage ?: sessionErrorMessage
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _formattedPhoneNumber = MutableStateFlow("")

    private var timerJob: Job? = null
    private var finalizeJob: Job? = null
    private var lastObservedVerificationId: String? = null

    init {
        observePhoneDraft()
    }

    fun setFormattedPhoneNumber(value: String) {
        _formattedPhoneNumber.value = value
    }

    fun verifyOtp(
        code: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (_uiState.value.loading) return

        viewModelScope.launch {
            clearActionError()
            _uiState.update { it.copy(loading = true) }
            try {
                val result = phoneVerifier.submitOtp(code)
                result
                    .onSuccess {
                        onSuccess()
                    }
                    .onFailure { error ->
                        Log.e("OTP", "OTP verification failed", error)
                        setActionError(error.localizedMessage ?: "Invalid verification code")
                        _uiState.update { it.copy(loading = false) }
                        onError(error)
                    }
            } catch (error: Throwable) {
                Log.e("OTP", "OTP verification failed", error)
                setActionError(error.localizedMessage ?: "Invalid verification code")
                _uiState.update { it.copy(loading = false) }
                onError(error)
            }
        }
    }

    fun finalizeVerifiedUser(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (finalizeJob?.isActive == true) return

        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            val error = IllegalStateException("Authenticated user is unavailable")
            setActionError(error.localizedMessage ?: "Unable to finish account setup")
            onError(error)
            return
        }

        finalizeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    actionErrorMessage = null
                )
            }

            try {
                withTimeout(USER_FINALIZE_TIMEOUT_MS) {
                    val idToken = firebaseUser.getIdToken(false).await().token
                        ?: throw IllegalStateException("Authenticated session token is unavailable")
                    val finalized = authPolicyHandler.finalizePhoneSignup(idToken)
                    if (!finalized) {
                        throw IllegalStateException("Unable to finish account setup. Please retry.")
                    }
                }
                phoneVerifier.cancel()
                onSuccess()
            } catch (error: TimeoutCancellationException) {
                Log.e("OTP", "Phone signup finalization timed out", error)
                val message = "Unable to finish account setup. Please retry."
                setActionError(message)
                onError(IllegalStateException(message, error))
            } catch (error: CancellationException) {
                Log.d("OTP", "Phone signup finalization cancelled", error)
                throw error
            } catch (error: Throwable) {
                Log.e("OTP", "Failed to finalize verified phone signup", error)
                setActionError(
                    error.localizedMessage ?: "Unable to finish account setup. Please retry."
                )
                onError(error)
            } finally {
                finalizeJob = null
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun resendOtp(
        phoneNumber: String,
        activity: Activity,
        onError: (Throwable) -> Unit
    ) {
        if (_uiState.value.isResending) return

        viewModelScope.launch {
            clearActionError()
            _uiState.update { it.copy(isResending = true) }

            val e164 = phoneDraftStore.draft.first().e164 ?: phoneNumber
            val result = phoneVerifier.resend(e164, activity)
            result.onFailure { error ->
                setActionError(error.localizedMessage ?: "Unable to resend OTP")
                onError(error)
            }

            _uiState.update { it.copy(isResending = false) }
        }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionErrorMessage = null) }
    }

    private fun observePhoneDraft() {
        viewModelScope.launch {
            phoneDraftStore.draft.collect { draft ->
                handleDraftUpdate(draft)
            }
        }
    }

    private fun handleDraftUpdate(draft: PhoneDraft) {
        val verificationId = draft.verificationId
        if (!verificationId.isNullOrBlank() && verificationId != lastObservedVerificationId) {
            lastObservedVerificationId = verificationId
            startTimer()
        }

        val awaitingCode = draft.e164 != null &&
            verificationId.isNullOrBlank() &&
            !draft.verified &&
            draft.errorMessage.isNullOrBlank()

        _uiState.update { current ->
            current.copy(
                verificationReady = !verificationId.isNullOrBlank(),
                awaitingCode = awaitingCode,
                verified = draft.verified,
                sessionErrorMessage = draft.errorMessage
            )
        }
    }

    private fun setActionError(message: String) {
        _uiState.update { it.copy(actionErrorMessage = message) }
    }

    private fun startTimer(durationSeconds: Int = 60) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (second in durationSeconds downTo 0) {
                _uiState.update {
                    it.copy(
                        remainingSeconds = second,
                        isResendAvailable = second == 0
                    )
                }
                delay(1000)
            }
        }
    }
}

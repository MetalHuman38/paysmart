package net.metalbrain.paysmart.phone

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReauthOtpViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier,
    private val phoneDraftStore: PhoneDraftStore
) : ViewModel() {


    private val _code = mutableStateOf("")
    val code: State<String> = _code

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading


    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false,
        val resendAvailable: Boolean = false,
        val timerSeconds: Int = 0
    )

    private var timerJob: Job? = null
    private var resendCount = 0

    fun startReauthFlow(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // ✅ Get cached phone number
                val phoneDraft = phoneDraftStore.draft.first()
                val phone = phoneDraft.e164
                Log.d("ReauthOtpViewModel", "📞 Phone: $phone")

                if (phone.isNullOrBlank()) {
                    _uiState.update { it.copy(error = "Phone number not available") }
                    return@launch
                }

                // ✅ Set callbacks before starting
                phoneVerifier.setCallbacks(
                    onCodeSent = { startTimer() },
                    onError = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "OTP failed") }
                    }
                )

                phoneVerifier.start(phone, activity)

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to start OTP") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }



    fun reauthWithCode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val verificationId = phoneVerifier.getVerificationId()
                    ?: throw IllegalStateException("Missing verification ID")

                val credential = PhoneAuthProvider.getCredential(verificationId, code.value)
                FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)


                // ❗ No currentUser — you should use this only in OTP login context
                val result = phoneVerifier.submitOtp(code.value)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(success = true) }
                        onSuccess()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message ?: "OTP failed") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Reauthentication failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun errorHandled() {
        _uiState.update { it.copy(error = null) }
    }


    fun onCodeChange(newCode: String) {
        _code.value = newCode
    }

    fun resendOtp(activity: Activity) {
        viewModelScope.launch {
            try {
                val phoneDraft = phoneDraftStore.draft.first()
                val phone = phoneDraft.e164 ?: return@launch
                phoneVerifier.resend(phone, activity)
                startTimer()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Failed to resend") }
            }
        }
    }

    private fun startTimer(duration: Int = 30) {
        timerJob?.cancel()
        val delaySeconds = (duration + (resendCount * 10)).coerceAtMost(60)
        resendCount++

        timerJob = viewModelScope.launch {
            for (i in delaySeconds downTo 0) {
                _uiState.update { it.copy(timerSeconds = i, resendAvailable = i == 0) }
                delay(1000)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

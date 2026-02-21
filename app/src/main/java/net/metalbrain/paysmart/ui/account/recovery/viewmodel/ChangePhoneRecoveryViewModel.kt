package net.metalbrain.paysmart.ui.account.recovery.viewmodel

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.PhoneChangePolicyHandler
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.ui.account.recovery.auth.ChangePhoneRecoveryAuthGateway
import javax.inject.Inject

@HiltViewModel
class ChangePhoneRecoveryViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val phoneChangePolicyHandler: PhoneChangePolicyHandler,
    private val authGateway: ChangePhoneRecoveryAuthGateway,
) : ViewModel() {

    data class UiState(
        val newPhoneNumber: String = "",
        val otpCode: String = "",
        val isLoading: Boolean = false,
        val isCodeSent: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var verificationId: String? = null
    private var pendingPhoneE164: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun onPhoneNumberChanged(value: String) {
        _uiState.update { it.copy(newPhoneNumber = value, error = null) }
    }

    fun onOtpChanged(value: String) {
        _uiState.update { it.copy(otpCode = value, error = null) }
    }

    fun sendCode(activity: FragmentActivity) {
        val normalized = normalizePhone(_uiState.value.newPhoneNumber)
        if (!isLikelyE164(normalized)) {
            _uiState.update { it.copy(error = "Enter a valid phone number in international format (+123...)") }
            return
        }

        if (!authGateway.hasAuthenticatedUser()) {
            _uiState.update { it.copy(error = "Sign in again before changing your phone number") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        pendingPhoneE164 = normalized

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch { applyCredential(credential) }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Phone verification failed"
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@ChangePhoneRecoveryViewModel.verificationId = verificationId
                resendToken = token
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCodeSent = true,
                        error = null
                    )
                }
            }
        }

        authGateway.startPhoneVerification(
            activity = activity,
            phoneNumber = normalized,
            resendToken = resendToken,
            callbacks = callbacks
        )
    }

    fun confirmCode() {
        val currentVerificationId = verificationId
        val code = _uiState.value.otpCode.trim()

        if (currentVerificationId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Please request a new OTP code") }
            return
        }
        if (code.length < 6) {
            _uiState.update { it.copy(error = "Enter the 6-digit OTP code") }
            return
        }

        val credential = authGateway.credentialFromCode(currentVerificationId, code)
        viewModelScope.launch { applyCredential(credential) }
    }

    private suspend fun applyCredential(credential: PhoneAuthCredential) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val session = authGateway.applyPhoneCredential(
                credential = credential,
                fallbackPhoneE164 = pendingPhoneE164
            )

            val serverAck = phoneChangePolicyHandler.confirmPhoneChanged(
                idToken = session.idToken,
                phoneNumber = session.phoneNumber
            )
            if (!serverAck) {
                throw IllegalStateException("Server failed to confirm phone number change")
            }

            userProfileRepository.updatePhoneNumber(session.uid, session.phoneNumber)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            }
        } catch (e: Exception) {
            Log.e("ChangePhoneRecoveryVM", "Phone change failed", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Unable to change phone number"
                )
            }
        }
    }

    private fun normalizePhone(raw: String): String {
        val compact = raw.trim().replace(" ", "")
        if (compact.startsWith("+")) {
            return compact
        }
        return "+$compact"
    }

    private fun isLikelyE164(phone: String): Boolean {
        return phone.matches(Regex("^\\+[1-9]\\d{7,14}$"))
    }
}

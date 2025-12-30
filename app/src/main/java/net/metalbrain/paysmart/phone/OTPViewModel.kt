package net.metalbrain.paysmart.phone

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.UserStatus

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier,
    private val auth: FirebaseAuth,
    private val userRepo: UserProfileRepository,
    private val phoneDraftStore: PhoneDraftStore
) : ViewModel() {
    private val _state = MutableStateFlow(OTPState())

    private val _formattedPhoneNumber = MutableStateFlow("")

    fun setFormattedPhoneNumber(value: String) {
        _formattedPhoneNumber.value = value
    }



    private var timerJob: Job? = null
    private var resendCount = 0

    fun startTimer(delaySeconds: Int = 30, backoff: Boolean = true) {
        timerJob?.cancel()
        val totalDelay = if (backoff) {
            (delaySeconds + resendCount * 10).coerceAtMost(60)
        } else delaySeconds

        resendCount++
        _state.value = _state.value.copy(code = "", remainingSeconds = totalDelay, isResendAvailable = false)

        timerJob = viewModelScope.launch {
            for (i in totalDelay downTo 0) {
                _state.update { it.copy(remainingSeconds = i, isResendAvailable = i == 0) }
                delay(1000)
            }
        }
    }

    fun verifyOtp(
        code: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = phoneVerifier.submitOtp(code)
                Log.d("OTP", "OTP verification result: $result")
                result.fold(
                    onSuccess = {
                        val phoneNumber = auth.currentUser?.phoneNumber
                        if (!phoneNumber.isNullOrBlank()) {
                            viewModelScope.launch {
                                phoneDraftStore.saveDraft(
                                    PhoneDraft(
                                        e164 = phoneNumber,
                                        verificationId = null,
                                        verified = true
                                    )
                                )
                            }
                        }
                        onSuccess()
                    },
                    onFailure = { e -> onError(e) }
                )
                timerJob?.cancel()
                _state.update { it.copy(code = "") }
                resendCount = 0
                phoneVerifier.cancel()
                _formattedPhoneNumber.value = ""
                _state.value = OTPState()
                Log.d("OTP", "OTP verification successful")
            } catch (e: Exception) {
                Log.e("OTP", "OTP verification failed", e)
                onError(e)
            }
        }
    }

    fun upsertUserAfterOtp(onDone: () -> Unit = {}) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return

        val authUser = AuthUserModel(
            uid = firebaseUser.uid,
            phoneNumber = firebaseUser.phoneNumber,
            isAnonymous = firebaseUser.isAnonymous,
            emailVerified = firebaseUser.isEmailVerified,
            providerIds = firebaseUser.providerData.map { it.providerId },
            status = UserStatus.Unverified,
            tenantId = firebaseUser.tenantId,
            photoURL = firebaseUser.photoUrl?.toString(),
            displayName = firebaseUser.displayName,
            email = firebaseUser.email,
        )

        viewModelScope.launch {
            userRepo.upsertNewUser(authUser, providerId = "phone")
            onDone()
        }
    }

    fun resendOtp(
        phoneNumber: String,
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                phoneVerifier.resend(phoneNumber, activity)
                startTimer()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateCode(code: String) {
        _state.update { it.copy(code = code) }
        if (code.length == 6) {
            timerJob?.cancel()
        }
    }
}

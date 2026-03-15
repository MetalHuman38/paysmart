package net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel

import android.app.Activity
import com.google.firebase.Timestamp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.core.features.account.security.mfa.remote.MfaEnrollmentPromptPolicyHandler
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaEnrollmentStatus
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaEnrollmentProvider
import net.metalbrain.paysmart.data.repository.AuthRepository

private data class MfaEnrollmentRuntimeState(
    val isStartingEnrollment: Boolean = false,
    val isSendingCode: Boolean = false,
    val hasSentCode: Boolean = false,
    val destinationHint: String? = null,
    val verificationCode: String = "",
    val isVerifyingCode: Boolean = false,
    val infoMessage: String? = null
)

data class MfaNudgeUiState(
    val loading: Boolean = true,
    val hasSkippedInitialPrompt: Boolean = false,
    val isSignedIn: Boolean = false,
    val hasVerifiedEmail: Boolean = false,
    val hasEnrolledFactor: Boolean = false,
    val supportsEnrollment: Boolean = true,
    val enrollmentBlockMessage: String? = null,
    val blockedActionLabel: String? = null,
    val isStartingEnrollment: Boolean = false,
    val isSendingCode: Boolean = false,
    val hasSentCode: Boolean = false,
    val destinationHint: String? = null,
    val verificationCode: String = "",
    val isVerifyingCode: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MfaNudgeViewModel @Inject constructor(
    private val securityPreference: SecurityPreference,
    private val mfaEnrollmentProvider: MfaEnrollmentProvider,
    private val authRepository: AuthRepository,
    private val mfaEnrollmentPromptPolicyHandler: MfaEnrollmentPromptPolicyHandler
) : ViewModel() {

    private val status = MutableStateFlow(
        MfaEnrollmentStatus(
            signedIn = false,
            emailVerified = false,
            hasEnrolledFactor = false
        )
    )
    private val runtime = MutableStateFlow(MfaEnrollmentRuntimeState())
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MfaNudgeUiState> = combine(
        securityPreference.localSecurityStateFlow,
        status,
        runtime,
        error
    ) { localState, currentStatus, currentRuntime, currentError ->
        MfaNudgeUiState(
            loading = false,
            hasSkippedInitialPrompt = localState.hasSkippedMfaEnrollmentPrompt,
            isSignedIn = currentStatus.signedIn,
            hasVerifiedEmail = currentStatus.emailVerified,
            hasEnrolledFactor = currentStatus.hasEnrolledFactor,
            supportsEnrollment = currentStatus.supportsEnrollment,
            enrollmentBlockMessage = currentStatus.enrollmentBlockMessage,
            blockedActionLabel = currentStatus.blockedActionLabel,
            isStartingEnrollment = currentRuntime.isStartingEnrollment,
            isSendingCode = currentRuntime.isSendingCode,
            hasSentCode = currentRuntime.hasSentCode,
            destinationHint = currentRuntime.destinationHint,
            verificationCode = currentRuntime.verificationCode,
            isVerifyingCode = currentRuntime.isVerifyingCode,
            infoMessage = currentRuntime.infoMessage,
            errorMessage = currentError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MfaNudgeUiState()
    )

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            runCatching { mfaEnrollmentProvider.loadStatus() }
                .onSuccess {
                    status.value = it
                    error.value = null
                    syncLocalMfaState(it.hasEnrolledFactor)
                    if (it.hasEnrolledFactor) {
                        runtime.value = runtime.value.copy(
                            hasSentCode = false,
                            verificationCode = "",
                            isStartingEnrollment = false,
                            isSendingCode = false,
                            isVerifyingCode = false,
                            infoMessage = "2-step verification enabled."
                        )
                        mfaEnrollmentProvider.clearPendingSession()
                        setSkippedPrompt(false)
                    }
                }
                .onFailure {
                    error.value = it.localizedMessage ?: "Unable to load MFA status"
                }
        }
    }

    fun skipInitialPrompt() {
        viewModelScope.launch {
            setSkippedPrompt(true)
        }
    }

    fun onVerificationCodeChanged(value: String) {
        val normalized = value.filter { it.isDigit() }.take(6)
        runtime.value = runtime.value.copy(verificationCode = normalized)
    }

    fun startSessionAndSendCode(activity: Activity) {
        if (runtime.value.isStartingEnrollment || runtime.value.isSendingCode) return

        viewModelScope.launch {
            error.value = null
            runtime.value = runtime.value.copy(
                isStartingEnrollment = true,
                isSendingCode = false,
                infoMessage = null
            )

            val startResult = mfaEnrollmentProvider.startSession()
            if (startResult.isFailure) {
                runtime.value = runtime.value.copy(isStartingEnrollment = false)
                error.value = startResult.exceptionOrNull()?.localizedMessage
                    ?: "Unable to start MFA enrollment session"
                return@launch
            }

            val sendResult = mfaEnrollmentProvider.sendVerificationCode(activity)
            sendResult
                .onSuccess { challenge ->
                    runtime.value = runtime.value.copy(
                        isStartingEnrollment = false,
                        isSendingCode = false,
                        hasSentCode = true,
                        destinationHint = challenge.destinationHint,
                        infoMessage = "Verification code sent.",
                        verificationCode = ""
                    )
                    markPromptHandledForSetup()
                }
                .onFailure {
                    runtime.value = runtime.value.copy(
                        isStartingEnrollment = false,
                        isSendingCode = false
                    )
                    error.value = it.localizedMessage ?: "Unable to send verification code"
                }
        }
    }

    fun resendCode(activity: Activity) {
        if (runtime.value.isSendingCode || runtime.value.isStartingEnrollment) return
        viewModelScope.launch {
            error.value = null
            runtime.value = runtime.value.copy(isSendingCode = true, infoMessage = null)
            mfaEnrollmentProvider.resendVerificationCode(activity)
                .onSuccess {
                    runtime.value = runtime.value.copy(
                        isSendingCode = false,
                        infoMessage = "Verification code resent."
                    )
                }
                .onFailure {
                    runtime.value = runtime.value.copy(isSendingCode = false)
                    error.value = it.localizedMessage ?: "Unable to resend verification code"
                }
        }
    }

    fun verifyCodeAndEnroll() {
        if (runtime.value.isVerifyingCode) return

        viewModelScope.launch {
            error.value = null
            runtime.value = runtime.value.copy(isVerifyingCode = true, infoMessage = null)
            mfaEnrollmentProvider.verifyCodeAndEnroll(runtime.value.verificationCode)
                .onSuccess {
                    runtime.value = runtime.value.copy(
                        isVerifyingCode = false,
                        infoMessage = "2-step verification enabled."
                    )
                    refreshStatus()
                }
                .onFailure {
                    runtime.value = runtime.value.copy(isVerifyingCode = false)
                    error.value = it.localizedMessage ?: "Unable to verify code and enroll MFA"
                }
        }
    }

    fun markPromptHandledForSetup() {
        viewModelScope.launch {
            setSkippedPrompt(false)
        }
    }

    private suspend fun setSkippedPrompt(skipped: Boolean) {
        val current = securityPreference.loadLocalSecurityState()
        val session = authRepository.getCurrentSession()
        if (session != null) {
            val synced = mfaEnrollmentPromptPolicyHandler.setPromptState(
                idToken = session.idToken,
                hasSkippedMfaEnrollmentPrompt = skipped
            )
            if (!synced) {
                error.value = "Saved locally. Server sync pending."
            }
        }
        securityPreference.saveLocalSecurityState(
            current.copy(
                hasSkippedMfaEnrollmentPrompt = skipped,
                lastSynced = System.currentTimeMillis()
            )
        )
    }

    private suspend fun syncLocalMfaState(hasEnrolledFactor: Boolean) {
        val current = securityPreference.loadLocalSecurityState()
        securityPreference.saveLocalSecurityState(
            current.copy(
                hasEnrolledMfaFactor = hasEnrolledFactor,
                mfaEnrolledAt = when {
                    hasEnrolledFactor -> current.mfaEnrolledAt ?: Timestamp.now()
                    else -> null
                },
                lastSynced = System.currentTimeMillis()
            )
        )
    }
}

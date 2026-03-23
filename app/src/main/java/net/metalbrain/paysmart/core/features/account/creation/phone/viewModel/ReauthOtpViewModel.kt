package net.metalbrain.paysmart.core.features.account.creation.phone.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthCredential
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
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraft
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraftStore
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraft
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraftStore
import net.metalbrain.paysmart.core.auth.providers.FacebookAuthProviderHelper
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.usecase.EmailLinkUseCase
import kotlinx.coroutines.tasks.await

@HiltViewModel
class ReauthOtpViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier,
    private val phoneDraftStore: PhoneDraftStore,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val emailDraftStore: EmailDraftStore,
    private val emailLinkUseCase: EmailLinkUseCase
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
        val infoMessage: String? = null,
        val success: Boolean = false,
        val resendAvailable: Boolean = false,
        val timerSeconds: Int = 0,
        val factorsResolved: Boolean = false,
        val hasPhoneFactor: Boolean = false,
        val recoveryEmail: String? = null,
        val canUseEmailLink: Boolean = false,
        val canUseGoogle: Boolean = false,
        val canUseFacebook: Boolean = false
    )

    private var timerJob: Job? = null
    private var resendCount = 0

    init {
        viewModelScope.launch {
            loadRecoveryFactors()
        }
    }

    private suspend fun resolvePhoneForReauth(): String? {
        val draft = phoneDraftStore.draft.first()
        draft.e164?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

        val currentUser = authRepository.currentUser
        currentUser?.phoneNumber
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { phone ->
                phoneDraftStore.saveDraft(draft.copy(e164 = phone))
                return phone
            }

        val uid = currentUser?.uid ?: return null
        val remotePhone = userProfileRepository.getOnce(uid)
            ?.phoneNumber
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (remotePhone != null) {
            phoneDraftStore.saveDraft(PhoneDraft(e164 = remotePhone))
        }

        return remotePhone
    }

    private suspend fun resolveEmailForReauth(): String? {
        val draft = emailDraftStore.draft.first()
        draft.email?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

        val currentUser = authRepository.currentUser
        currentUser?.email
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { email ->
                emailDraftStore.saveDraft(EmailDraft(email = email, verified = true))
                return email
            }

        val uid = currentUser?.uid ?: return null
        val remoteEmail = userProfileRepository.getOnce(uid)
            ?.email
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (remoteEmail != null) {
            emailDraftStore.saveDraft(EmailDraft(email = remoteEmail, verified = true))
        }

        return remoteEmail
    }

    private suspend fun loadRecoveryFactors() {
        _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
        try {
            val currentUser = authRepository.currentUser
            val providerIds = currentUser
                ?.providerData
                ?.mapNotNull { it.providerId }
                ?.toSet()
                .orEmpty()
            val phone = resolvePhoneForReauth()
            val email = resolveEmailForReauth()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    factorsResolved = true,
                    hasPhoneFactor = !phone.isNullOrBlank(),
                    recoveryEmail = email,
                    canUseEmailLink = !email.isNullOrBlank() && providerIds.contains("password"),
                    canUseGoogle = providerIds.contains("google.com"),
                    canUseFacebook = providerIds.contains("facebook.com")
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    factorsResolved = true,
                    error = e.localizedMessage ?: "Failed to load recovery options"
                )
            }
        }
    }

    private suspend fun reauthenticateWithCredential(
        credential: AuthCredential,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("User not authenticated")
        user.reauthenticate(credential).await()
        _uiState.update { it.copy(success = true, error = null) }
        onSuccess()
    }

    fun startReauthFlow(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }

            try {
                val phone = resolvePhoneForReauth()
                Log.d("ReauthOtpViewModel", "Resolved phone for OTP reauth")

                if (phone.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasPhoneFactor = false,
                            factorsResolved = true
                        )
                    }
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
                reauthenticateWithCredential(credential, onSuccess)
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

    fun infoHandled() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun reportError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun onCodeChange(newCode: String) {
        _code.value = newCode
    }

    fun sendEmailLink(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
            try {
                val email = resolveEmailForReauth()
                    ?: throw IllegalStateException("Recovery email is not available")
                emailDraftStore.saveDraft(EmailDraft(email = email, verified = true))
                emailLinkUseCase.sendMagicLink(context, email)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recoveryEmail = email,
                        infoMessage = "Verification link sent to $email"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to send email link"
                    )
                }
            }
        }
    }

    fun handleEmailReauthIntent(intent: Intent, onSuccess: () -> Unit) {
        val emailLink = intent.data?.toString()?.trim().orEmpty()
        if (emailLink.isBlank()) return
        val auth = FirebaseAuth.getInstance()
        if (!auth.isSignInWithEmailLink(emailLink)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
            try {
                val user = auth.currentUser
                    ?: throw IllegalStateException("User not authenticated")
                val email = resolveEmailForReauth()
                    ?: throw IllegalStateException("Recovery email is not available")
                val credential = EmailAuthProvider.getCredentialWithLink(email, emailLink)
                user.reauthenticate(credential).await()
                _uiState.update { it.copy(isLoading = false, success = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Email reauthentication failed"
                    )
                }
            }
        }
    }

    fun reauthenticateWithGoogle(
        credential: AuthCredential,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
            try {
                reauthenticateWithCredential(credential, onSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Google reauthentication failed"
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reauthenticateWithFacebook(
        activity: Activity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
            try {
                val credential = FacebookAuthProviderHelper.loginWithFacebook(activity)
                reauthenticateWithCredential(credential, onSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Facebook reauthentication failed"
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resendOtp(activity: Activity) {
        viewModelScope.launch {
            try {
                val phone = resolvePhoneForReauth() ?: return@launch
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

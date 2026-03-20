package net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaSignInFactorOption
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaSignInProvider

data class MfaSignInUiState(
    val loading: Boolean = true,
    val hasPendingChallenge: Boolean = false,
    val factors: List<MfaSignInFactorOption> = emptyList(),
    val selectedFactorUid: String? = null,
    val hasSentCode: Boolean = false,
    val destinationHint: String? = null,
    val verificationCode: String = "",
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val isComplete: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MfaSignInViewModel @Inject constructor(
    private val mfaSignInProvider: MfaSignInProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MfaSignInUiState())
    val uiState: StateFlow<MfaSignInUiState> = _uiState

    init {
        refreshPendingChallenge()
    }

    fun refreshPendingChallenge() {
        val pendingChallenge = mfaSignInProvider.getPendingChallenge()
        _uiState.value = if (pendingChallenge == null) {
            MfaSignInUiState(
                loading = false,
                errorMessage = "This verification session expired. Start sign-in again."
            )
        } else {
            MfaSignInUiState(
                loading = false,
                hasPendingChallenge = true,
                factors = pendingChallenge.factors,
                selectedFactorUid = pendingChallenge.selectedFactorUid,
                destinationHint = pendingChallenge.destinationHint
            )
        }
    }

    fun onFactorSelected(factorUid: String) {
        if (_uiState.value.isSendingCode || _uiState.value.isVerifyingCode) return
        mfaSignInProvider.selectFactor(factorUid)
            .onSuccess { challenge ->
                _uiState.update {
                    it.copy(
                        factors = challenge.factors,
                        selectedFactorUid = challenge.selectedFactorUid,
                        destinationHint = challenge.destinationHint,
                        hasSentCode = false,
                        verificationCode = "",
                        infoMessage = null,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Unable to update verification method."
                    )
                }
            }
    }

    fun onVerificationCodeChanged(value: String) {
        val normalized = value.filter { it.isDigit() }.take(6)
        _uiState.update {
            it.copy(
                verificationCode = normalized,
                errorMessage = null
            )
        }
    }

    fun sendCode(activity: Activity) {
        val currentState = _uiState.value
        if (!currentState.hasPendingChallenge || currentState.isSendingCode || currentState.isVerifyingCode) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            mfaSignInProvider.sendVerificationCode(activity)
                .onSuccess { challenge ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            hasSentCode = true,
                            factors = challenge.factors,
                            selectedFactorUid = challenge.selectedFactorUid,
                            destinationHint = challenge.destinationHint,
                            verificationCode = "",
                            infoMessage = "Verification code sent."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            errorMessage = error.localizedMessage ?: "Unable to send verification code."
                        )
                    }
                }
        }
    }

    fun resendCode(activity: Activity) {
        val currentState = _uiState.value
        if (!currentState.hasPendingChallenge || currentState.isSendingCode || currentState.isVerifyingCode) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            mfaSignInProvider.resendVerificationCode(activity)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            infoMessage = "Verification code resent."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            errorMessage = error.localizedMessage ?: "Unable to resend verification code."
                        )
                    }
                }
        }
    }

    fun verifyCodeAndSignIn() {
        val currentState = _uiState.value
        if (!currentState.hasPendingChallenge || currentState.isVerifyingCode) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVerifyingCode = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            mfaSignInProvider.verifyCodeAndSignIn(currentState.verificationCode)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isComplete = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            errorMessage = error.localizedMessage ?: "Unable to complete sign-in."
                        )
                    }
                }
        }
    }

    fun clearPendingChallenge() {
        mfaSignInProvider.clearPendingChallenge()
        _uiState.update {
            it.copy(hasPendingChallenge = false)
        }
    }
}

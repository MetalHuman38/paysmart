package net.metalbrain.paysmart.core.features.account.passkey.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyApiRepository
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyCredentialManager
import net.metalbrain.paysmart.core.features.account.passkey.state.PasskeySetupUiState
import net.metalbrain.paysmart.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class PasskeySetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val passkeyApiRepository: PasskeyApiRepository,
    private val passkeyCredentialManager: PasskeyCredentialManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasskeySetupUiState())
    val uiState: StateFlow<PasskeySetupUiState> = _uiState.asStateFlow()

    fun registerPasskey(activity: Activity) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, statusMessage = "Preparing passkey registration...") }
            try {
                val session = authRepository.getCurrentSessionOrThrow()
                val userName = session.user.email.orEmpty().ifBlank { session.user.uid }
                val displayName = session.user.displayName.orEmpty().ifBlank { userName }

                val options = passkeyApiRepository
                    .fetchRegistrationOptions(userName, displayName)
                    .getOrThrow()
                val credentialJson = passkeyCredentialManager
                    .createPasskey(activity, options)
                    .getOrThrow()
                val verified = passkeyApiRepository
                    .verifyRegistration(credentialJson)
                    .getOrThrow()
                if (!verified) {
                    throw IllegalStateException("Passkey registration verification failed")
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = true,
                        statusMessage = "Passkey registered on this device.",
                        error = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = null,
                        error = error.localizedMessage ?: "Unable to register passkey"
                    )
                }
            }
        }
    }

    fun verifyPasskey(activity: Activity) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, statusMessage = "Verifying passkey...") }
            try {
                val options = passkeyApiRepository.fetchAuthenticationOptions().getOrThrow()
                val assertionJson = passkeyCredentialManager.getAssertion(activity, options).getOrThrow()
                val verified = passkeyApiRepository.verifyAuthentication(assertionJson).getOrThrow()
                if (!verified) {
                    throw IllegalStateException("Passkey authentication verification failed")
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Passkey verification succeeded.",
                        error = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = null,
                        error = error.localizedMessage ?: "Unable to verify passkey"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

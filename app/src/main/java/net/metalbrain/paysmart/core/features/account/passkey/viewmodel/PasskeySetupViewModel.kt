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
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.data.repository.AuthRepository
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class PasskeySetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val passkeyApiRepository: PasskeyApiRepository,
    private val passkeyCredentialManager: PasskeyCredentialManager,
    private val securityPreference: SecurityPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasskeySetupUiState())
    val uiState: StateFlow<PasskeySetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val local = securityPreference.loadLocalSecurityState()
            _uiState.update { it.copy(isRegistered = local.passkeyEnabled) }
        }
    }

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
                val rpId = parseRpId(options)
                if (!rpId.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(statusMessage = "Preparing passkey registration (rp.id=$rpId)...")
                    }
                }
                val credentialJson = passkeyCredentialManager
                    .createPasskey(activity, options)
                    .getOrThrow()
                val verified = passkeyApiRepository
                    .verifyRegistration(credentialJson)
                    .getOrThrow()
                if (!verified) {
                    throw IllegalStateException("Passkey registration verification failed")
                }

                val synced = passkeyApiRepository.setPasskeyEnabled(true).getOrElse { false }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = true,
                        statusMessage = if (synced) {
                            "Passkey registered on this device."
                        } else {
                            "Passkey registered. Server sync pending."
                        },
                        error = null
                    )
                }
                saveLocalPasskeyState(enabled = true)
            } catch (error: Exception) {
                val rawMessage = error.localizedMessage ?: "Unable to register passkey"
                val rpIdFromState = parseRpIdFromStatus(_uiState.value.statusMessage)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = buildRegistrationErrorHint(rawMessage, rpIdFromState)
                    )
                }
            }
        }
    }

    fun disablePasskey() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    statusMessage = "Disabling passkey..."
                )
            }

            val session = authRepository.getCurrentSession()
            if (session != null) {
                val synced = passkeyApiRepository.setPasskeyEnabled(false).getOrElse {
                    false
                }
                if (!synced) {
                    _uiState.update {
                        it.copy(error = "Saved locally. Server sync pending.")
                    }
                }
            }

            saveLocalPasskeyState(enabled = false)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRegistered = false,
                    statusMessage = "Passkey disabled for this account.",
                    error = it.error
                )
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

    private fun parseRpId(optionsJson: String): String? {
        return runCatching {
            JSONObject(optionsJson)
                .optJSONObject("rp")
                ?.optString("id")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun parseRpIdFromStatus(statusMessage: String?): String? {
        val message = statusMessage ?: return null
        val marker = "rp.id="
        val start = message.indexOf(marker)
        if (start < 0) return null
        val rp = message.substring(start + marker.length).substringBefore(")").trim()
        return rp.takeIf { it.isNotBlank() }
    }

    private fun buildRegistrationErrorHint(rawMessage: String, rpId: String?): String {
        val normalized = rawMessage.lowercase()
        if (
            normalized.contains("rp id cannot be validated") ||
            normalized.contains("rp.id cannot be validated")
        ) {
            val rpSuffix = rpId?.let { " (rp.id=$it)" }.orEmpty()
            return "Passkey RP validation failed$rpSuffix. Check Digital Asset Links and include debug/release app cert SHA-256 fingerprints in public/.well-known/assetlinks.json."
        }
        return rawMessage
    }

    private suspend fun saveLocalPasskeyState(enabled: Boolean) {
        val current = securityPreference.loadLocalSecurityState()
        securityPreference.saveLocalSecurityState(
            current.copy(
                passkeyEnabled = enabled,
                hasSkippedPasskeyEnrollmentPrompt = !enabled,
                lastSynced = System.currentTimeMillis()
            )
        )
    }
}

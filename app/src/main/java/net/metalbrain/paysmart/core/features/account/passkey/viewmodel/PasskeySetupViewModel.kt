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
        bootstrapState()
    }

    fun registerPasskey(activity: Activity) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    statusMessage = "Preparing passkey registration..."
                )
            }
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
                saveLocalPasskeyState(enabled = true)

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
                refreshCredentialsInternal(silent = true)
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
                    statusMessage = "Disabling passkey and revoking credentials..."
                )
            }

            val credentialsToRevoke = passkeyApiRepository.listCredentials().getOrElse { emptyList() }
            var revokedCount = 0
            var failedCount = 0

            credentialsToRevoke.forEach { credential ->
                val revoked = passkeyApiRepository.revokeCredential(credential.credentialId).getOrElse { false }
                if (revoked) revokedCount += 1 else failedCount += 1
            }

            val session = authRepository.getCurrentSession()
            val synced = if (session == null) {
                false
            } else {
                passkeyApiRepository.setPasskeyEnabled(false).getOrElse { false }
            }

            saveLocalPasskeyState(enabled = false)
            val refreshedCredentials = passkeyApiRepository.listCredentials().getOrElse { emptyList() }

            val baseStatus = when {
                credentialsToRevoke.isEmpty() -> "Passkey disabled for this account."
                failedCount == 0 -> "Passkey disabled. Revoked $revokedCount credential(s)."
                else -> "Passkey disabled with partial revoke: $revokedCount succeeded, $failedCount failed."
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRegistered = false,
                    credentials = refreshedCredentials,
                    activeRevokeCredentialId = null,
                    statusMessage = if (!synced && session != null) {
                        "$baseStatus Server sync pending."
                    } else {
                        baseStatus
                    },
                    error = if (failedCount > 0) {
                        "Some credentials could not be revoked. Try again from device list."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun revokeCredential(credentialId: String) {
        if (_uiState.value.isLoading || _uiState.value.activeRevokeCredentialId != null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeRevokeCredentialId = credentialId,
                    error = null,
                    statusMessage = null
                )
            }

            val revoked = passkeyApiRepository.revokeCredential(credentialId).getOrElse { false }
            if (!revoked) {
                _uiState.update {
                    it.copy(
                        activeRevokeCredentialId = null,
                        error = "Unable to revoke credential. Try again."
                    )
                }
                return@launch
            }

            val refreshedCredentials = passkeyApiRepository.listCredentials().getOrElse { emptyList() }
            val allRemoved = refreshedCredentials.isEmpty()
            if (allRemoved && _uiState.value.isRegistered) {
                passkeyApiRepository.setPasskeyEnabled(false)
                saveLocalPasskeyState(enabled = false)
            }

            _uiState.update {
                it.copy(
                    activeRevokeCredentialId = null,
                    isRegistered = if (allRemoved) false else it.isRegistered,
                    credentials = refreshedCredentials,
                    statusMessage = if (allRemoved) {
                        "All passkeys removed from this account."
                    } else {
                        "Credential revoked."
                    },
                    error = null
                )
            }
        }
    }

    fun refreshCredentialList() {
        viewModelScope.launch {
            refreshCredentialsInternal(silent = false)
        }
    }

    fun verifyPasskey(activity: Activity) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    statusMessage = "Verifying passkey..."
                )
            }
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

    private fun bootstrapState() {
        viewModelScope.launch {
            val local = securityPreference.loadLocalSecurityState()
            _uiState.update { it.copy(isRegistered = local.passkeyEnabled) }
            refreshCredentialsInternal(silent = true)
        }
    }

    private suspend fun refreshCredentialsInternal(silent: Boolean) {
        val session = authRepository.getCurrentSession()
        if (session == null) {
            _uiState.update { it.copy(credentials = emptyList()) }
            return
        }

        passkeyApiRepository.listCredentials()
            .onSuccess { credentials ->
                _uiState.update {
                    it.copy(
                        credentials = credentials
                    )
                }
            }
            .onFailure { error ->
                if (!silent) {
                    _uiState.update {
                        it.copy(error = error.localizedMessage ?: "Unable to load credentials")
                    }
                }
            }
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

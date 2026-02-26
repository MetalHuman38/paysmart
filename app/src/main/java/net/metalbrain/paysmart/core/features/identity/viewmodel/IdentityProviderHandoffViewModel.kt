package net.metalbrain.paysmart.core.features.identity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.identity.handoff.IdentityProviderHandoffRepository
import net.metalbrain.paysmart.core.features.identity.handoff.IdentityProviderSdkCallback
import net.metalbrain.paysmart.core.features.identity.handoff.IdentityProviderSessionStart
import net.metalbrain.paysmart.core.features.identity.state.IdentityProviderHandoffUiState

@HiltViewModel
class IdentityProviderHandoffViewModel @Inject constructor(
    private val repository: IdentityProviderHandoffRepository
) : ViewModel() {

    private var lastCallbackSignature: String? = null
    private val _uiState = MutableStateFlow(IdentityProviderHandoffUiState())
    val uiState: StateFlow<IdentityProviderHandoffUiState> = _uiState.asStateFlow()

    fun startSession(countryIso2: String?, documentType: String?) {
        if (_uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStartingSession = true, error = null, info = null) }
            repository.startSession(IdentityProviderSessionStart(countryIso2, documentType))
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isStartingSession = false,
                            sessionId = session.sessionId,
                            provider = session.provider,
                            status = session.status,
                            launchUrl = session.launchUrl,
                            reason = null,
                            info = "Provider session created"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isStartingSession = false,
                            error = error.localizedMessage ?: "Unable to start provider session"
                        )
                    }
                }
        }
    }

    fun resumeSession() {
        val sessionId = _uiState.value.sessionId
        if (_uiState.value.isBusy || sessionId.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResuming = true, error = null, info = null) }
            repository.resumeSession(sessionId)
                .onSuccess { resume -> updateFromResume(resume.status, resume.launchUrl, resume.reason) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isResuming = false, error = error.localizedMessage ?: "Unable to resume session")
                    }
                }
        }
    }

    fun consumeCallbackArgs(event: String, sessionId: String?, providerRef: String?, deepLink: String?) {
        val signature = "$event|${sessionId.orEmpty()}|${providerRef.orEmpty()}|${deepLink.orEmpty()}"
        if (event.isBlank() || lastCallbackSignature == signature) return
        lastCallbackSignature = signature
        submitSdkCallback(IdentityProviderSdkCallback(event, sessionId, providerRef, deepLink))
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, info = null) }
    }

    private fun submitSdkCallback(callback: IdentityProviderSdkCallback) {
        if (_uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingCallback = true,
                    lastEvent = callback.event,
                    error = null,
                    info = null,
                    sessionId = callback.sessionId ?: it.sessionId
                )
            }
            repository.submitSdkCallback(callback)
                .onSuccess { resume -> updateFromResume(resume.status, resume.launchUrl, resume.reason) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmittingCallback = false,
                            error = error.localizedMessage ?: "Unable to submit callback"
                        )
                    }
                }
        }
    }

    private fun updateFromResume(status: String, launchUrl: String?, reason: String?) {
        _uiState.update {
            it.copy(
                isResuming = false,
                isSubmittingCallback = false,
                status = status,
                launchUrl = launchUrl ?: it.launchUrl,
                reason = reason,
                info = "Session status refreshed"
            )
        }
    }
}

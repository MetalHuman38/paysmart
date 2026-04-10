package net.metalbrain.paysmart.core.features.account.security.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.authorization.biometric.repository.BiometricRepository
import net.metalbrain.paysmart.core.features.account.security.data.SecurityPreference
import net.metalbrain.paysmart.core.features.account.security.manager.SecuritySyncManager
import net.metalbrain.paysmart.core.session.BaseSessionUseCase
import net.metalbrain.paysmart.core.session.SessionState
import net.metalbrain.paysmart.core.session.SessionStateManager
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase

internal fun resolvePostAuthState(
    authState: AuthState,
    localState: LocalSecuritySettingsModel,
    cloudState: SecuritySettingsModel?,
    sessionState: SessionState
): PostAuthState {
    return when (authState) {
        AuthState.Loading ->
            PostAuthState.Loading

        AuthState.Unauthenticated ->
            PostAuthState.Unauthenticated

        is AuthState.Authenticated -> {
            val isPasswordReady =
                localState.passwordEnabled && localState.localPasswordSetAt != null
            val recoveryMethodReady =
                localState.recoveryMethodReady ||
                    localState.hasVerifiedEmail ||
                    cloudState?.hasVerifiedEmail == true ||
                    isPasswordReady || // backward compat: local password set implies recovery was done
                    cloudState?.passwordEnabled == true // cloud password enabled means user previously registered
            val requiresPasswordRecovery =
                cloudState?.passwordEnabled == true && !isPasswordReady

            when {
                !recoveryMethodReady ->
                    PostAuthState.RequireRecoveryMethod

                requiresPasswordRecovery ->
                    PostAuthState.RequirePasswordRecovery

                !isPasswordReady ->
                    PostAuthState.RequireRecoveryPassword

                sessionState is SessionState.Locked ->
                    PostAuthState.Locked

                else ->
                    PostAuthState.Ready
            }
        }
    }
}

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val useCase: SecurityUseCase,
    private val syncManager: SecuritySyncManager,
    private val baseSessionUseCase: BaseSessionUseCase,
    private val securityPreference: SecurityPreference,
    private val biometricRepository: BiometricRepository,
    private val userManager: UserManager,
    sessionStateManager: SessionStateManager
) : ViewModel() {
    private var lastInteractionHeartbeatAt = 0L

    init {
        viewModelScope.launch {
            userManager.authState.collect { auth ->
                if (auth is AuthState.Authenticated) {
                    initializeSession()
                }
            }
        }
    }

    val postAuthState: StateFlow<PostAuthState> =
        combine(
            userManager.authState,
            securityPreference.localSecurityStateFlow,
            securityPreference.cloudSecuritySettingsFlow,
            sessionStateManager.sessionState
        ) { authState, localState, cloudState, sessionState ->
            resolvePostAuthState(
                authState = authState,
                localState = localState,
                cloudState = cloudState,
                sessionState = sessionState
            )
        }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PostAuthState.Loading
        )


    val localSecurityState: StateFlow<LocalSecurityState> =
        securityPreference.localSecurityStateFlow
            .map { LocalSecurityState.Ready(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LocalSecurityState.Loading
            )

    val hideBalanceEnabled: StateFlow<Boolean> =
        securityPreference.hideBalanceFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    val privacyCreditEnabled: StateFlow<Boolean> =
        securityPreference.privacyCreditEnabledFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    val privacySocialMediaEnabled: StateFlow<Boolean> =
        securityPreference.privacySocialMediaEnabledFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    fun initializeSession() {
        viewModelScope.launch {
            try {
                val (uid, token) = baseSessionUseCase.currentUserAndToken()
                syncManager.syncSecuritySettings(uid, token)
            } catch (e: Exception) {
                // handle safely
                e.printStackTrace()
            }
        }
    }

    suspend fun verify(passcode: String): Boolean =
        useCase.verifyPasscode(passcode)

    fun registerInteractionHeartbeat() {
        val now = System.currentTimeMillis()
        if (now - lastInteractionHeartbeatAt < 15_000L) {
            return
        }
        lastInteractionHeartbeatAt = now
        viewModelScope.launch {
            // Activity heartbeat should not change locked/unlocked state.
            securityPreference.updateLastUnlock()
        }
    }

    fun setHideBalance(enabled: Boolean) {
        viewModelScope.launch {
            securityPreference.setHideBalance(enabled)
        }
    }

    fun setPrivacyCreditEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreference.setPrivacyCreditEnabled(enabled)
        }
    }

    fun setPrivacySocialMediaEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreference.setPrivacySocialMediaEnabled(enabled)
        }
    }

    fun clearBiometricOptIn() {
        viewModelScope.launch {
            biometricRepository.clearBiometric()
        }
    }

    fun markRecoveryMethodReady() {
        viewModelScope.launch {
            securityPreference.markRecoveryMethodReady()
        }
    }
}

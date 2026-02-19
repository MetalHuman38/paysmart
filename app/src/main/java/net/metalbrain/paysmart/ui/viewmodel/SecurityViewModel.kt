package net.metalbrain.paysmart.ui.viewmodel

import androidx.fragment.app.FragmentActivity
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
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.core.security.SecuritySyncManager
import net.metalbrain.paysmart.core.session.BaseSessionUseCase
import net.metalbrain.paysmart.core.session.SessionState
import net.metalbrain.paysmart.core.session.SessionStateManager
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.security.SecurityPolicyEngine
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val useCase: SecurityUseCase,
    private val syncManager: SecuritySyncManager,
    private val baseSessionUseCase: BaseSessionUseCase,
    private val securityPreference: SecurityPreference,
    private val policyEngine: SecurityPolicyEngine,
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
            sessionStateManager.sessionState
        ) { authState, localState, sessionState ->

            when (authState) {

                AuthState.Loading ->
                    PostAuthState.Loading

                AuthState.Unauthenticated ->
                    PostAuthState.Unauthenticated

                is AuthState.Authenticated -> {

                    when {

                        // 🔴 User must configure protection
                        !localState.passcodeEnabled &&
                                !localState.biometricsEnabled &&
                                !localState.passwordEnabled ->
                            PostAuthState.RequireAccountProtection

                        // User must have verified email
                        !localState.hasVerifiedEmail ->
                            PostAuthState.RequireEmailVerification


                        // 🔒 Session locked
                        sessionState is SessionState.Locked ->
                            PostAuthState.Locked

                        else ->
                            PostAuthState.Ready
                    }
                }
            }
        }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PostAuthState.Loading
        )



    val localSecuritySettings: StateFlow<LocalSecuritySettingsModel?> =
        useCase.localSettingsFlow

    val localSecurityState: StateFlow<LocalSecurityState> =
        securityPreference.localSecurityStateFlow
            .map { LocalSecurityState.Ready(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LocalSecurityState.Loading
            )

    val isLocked: StateFlow<Boolean> = policyEngine.currentState
        .map { it.sessionLocked }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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

    suspend fun checkIfLocked() {
        val shouldLock = policyEngine.evaluateSecurityPolicy()
        if (shouldLock) {
            policyEngine.lockSession()
        }
    }



    suspend fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: (() -> Unit)? = null,
        onFailure: (() -> Unit)? = null
    ) {
        policyEngine.promptBiometric(
            activity = activity,
            onSuccess = {
                onSuccess?.invoke()
            },
            onFailure = {
                onFailure?.invoke()
            }
        )
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
}

package net.metalbrain.paysmart.domain.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.core.auth.BiometricHelper
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.utils.AppCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPolicyEngineImpl @Inject constructor(
    private val prefs: SecurityPreference,
    @param:AppCoroutineScope private val appScope: CoroutineScope
) : SecurityPolicyEngine {

    private val mutex = Mutex()

    private val _state = MutableStateFlow(LocalSecuritySettingsModel())
    override val currentState: StateFlow<LocalSecuritySettingsModel> = _state.asStateFlow()

    init {

        // Load initial state from DataStore
        appScope.launch {
            _state.value = prefs.loadLocalSecurityState()
        }

        // Keep state in sync with DataStore
        prefs.localSecurityStateFlow
            .onEach { _state.value = it }
            .launchIn(appScope)
    }

    override suspend fun evaluateSecurityPolicy(): Boolean = mutex.withLock {

        val state = _state.value

        // 🚨 HARD LOCK CONDITIONS (cannot proceed)
        if (state.killSwitchActive) return@withLock true

        if (state.biometricsRequired && !prefs.hasBiometricAuth()) return@withLock true

        if (state.passcodeEnabled && !prefs.hasPasscode()) return@withLock true


        // ⏳ IDLE LOCK CONDITION
        if (state.lockAfterMinutes != null && state.lockAfterMinutes!! > 0) {
            if (prefs.shouldAutoLock()) {
                return@withLock true
            }
        }

        return@withLock false
    }


    override suspend fun lockSession() = mutex.withLock {
        updateState(_state.value.copy(sessionLocked = true))
    }

    override suspend fun unlockSession() = mutex.withLock {
        updateState(_state.value.copy(sessionLocked = false))
    }

    suspend fun updateState(newState: LocalSecuritySettingsModel) {
        prefs.saveLocalSecurityState(newState)
        _state.value = newState
    }

    override suspend fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (() -> Unit)?
    ) {

        BiometricHelper.showPrompt(
            activity = activity,
            title = "Use biometric to unlock",
            subtitle = "Verify your identity to continue",
            onSuccess = {
                activity.lifecycleScope.launch {
                    prefs.updateLastUnlock()
                    onSuccess()
                }
            },
            onError = {
                onFailure?.invoke()
            },
            onFailureLimitReached = {
                onFailure?.invoke()
            }
        )
    }


    override fun isKillSwitchActive(): Boolean = _state.value.killSwitchActive

    override fun isBiometricRequired(): Boolean = _state.value.biometricsRequired

    override fun isPasscodeRequired(): Boolean = _state.value.passcodeEnabled
}

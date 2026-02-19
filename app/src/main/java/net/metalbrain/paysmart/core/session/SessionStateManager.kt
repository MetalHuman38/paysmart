package net.metalbrain.paysmart.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.domain.room.RoomUseCase
import net.metalbrain.paysmart.domain.security.SecurityPolicyEngine
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionStateManager @Inject constructor(
    private val securityUseCase: SecurityUseCase,
    private val policy: SecurityPolicyEngine,
    private val roomUseCase: RoomUseCase,
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun start(applicationScope: CoroutineScope) {
        applicationScope.launch(Dispatchers.Default) {
            observeAndSyncSession()
        }
    }

    suspend fun unlockSession() {
        securityUseCase.unlockSession()
        _sessionState.value = SessionState.Unlocked
    }

    suspend fun lockSession() {
        policy.lockSession()
        _sessionState.value = SessionState.Locked
    }

    private suspend fun observeAndSyncSession() {
        while (true) {
            try {
                val shouldLock = policy.evaluateSecurityPolicy()
                if (shouldLock && !policy.currentState.value.sessionLocked) {
                    policy.lockSession()
                }
                val isLocked = securityUseCase.isLocked() || shouldLock
                val isRoomKeyReady = roomUseCase.isReady()

                if (!isLocked && isRoomKeyReady) {
                    _sessionState.value = SessionState.Unlocked
                } else {
                    _sessionState.value = SessionState.Locked
                }
            } catch (e: Exception) {
                _sessionState.value = SessionState.Error("Unexpected session error: ${e.localizedMessage}")
            }

            delay(1_000)
        }
    }
}

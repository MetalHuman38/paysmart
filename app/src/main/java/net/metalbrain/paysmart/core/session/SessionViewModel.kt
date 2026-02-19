package net.metalbrain.paysmart.core.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthSession
import net.metalbrain.paysmart.domain.model.AuthUserModel

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val baseSessionUseCase: BaseSessionUseCase,
    private val sessionStateManager: SessionStateManager
) : ViewModel() {

    val sessionState = MutableStateFlow<AuthSession?>(null)



    val session: StateFlow<SessionState> =
        sessionStateManager.sessionState

    private val _user = MutableStateFlow<AuthUserModel?>(null)
    val user: StateFlow< AuthUserModel?> = _user.asStateFlow()

    init {
        refreshSession()
    }


    fun refreshSession() {
        viewModelScope.launch {
            sessionState.value = runCatching {
                baseSessionUseCase.currentSession()
            }.getOrNull()
        }
    }

    fun clearSession() {
        sessionState.value = null
    }

    fun isAuthenticated(): Boolean = sessionState.value != null

    fun unlockSession(onUnlocked: (() -> Unit)? = null) {
        viewModelScope.launch {
            sessionStateManager.unlockSession()
            onUnlocked?.invoke()
        }
    }

    fun lockSession() {
        viewModelScope.launch {
            sessionStateManager.lockSession()
        }
    }
}

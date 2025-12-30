package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityManager: SecuritySettingsManager
) : ViewModel() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    fun checkIfLocked() {
        _isLocked.value = securityManager.isLocked()
    }

    fun unlockSession() {
        securityManager.unlockSession()
        _isLocked.value = false
    }

    fun verify(passcode: String): Boolean = securityManager.verifyPasscode(passcode)

    fun hasPasscode(): Boolean = securityManager.hasPasscode()

    fun clearPasscode() = securityManager.clearPasscode()
}

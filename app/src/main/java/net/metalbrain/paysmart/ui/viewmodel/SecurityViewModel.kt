package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.metalbrain.paysmart.core.security.SecurityPrefs
import net.metalbrain.paysmart.data.repository.PasscodeRepository

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val passcodeRepository: PasscodeRepository
) : ViewModel() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    fun checkIfLocked(idleThresholdMinutes: Int) {
        val lastUnlock = SecurityPrefs.lastUnlockTimestamp
        val now = System.currentTimeMillis()
        val timeoutMillis = idleThresholdMinutes * 60 * 1000

        _isLocked.value = (now - lastUnlock) > timeoutMillis && passcodeRepository.hasPasscode()
    }

    fun unlockSession() {
        SecurityPrefs.lastUnlockTimestamp = System.currentTimeMillis()
        _isLocked.value = false
    }

    fun clearPasscode() {
        passcodeRepository.clear()
    }

    fun hasPasscode(): Boolean = passcodeRepository.hasPasscode()

    fun verify(passcode: String): Boolean = passcodeRepository.verify(passcode)
}

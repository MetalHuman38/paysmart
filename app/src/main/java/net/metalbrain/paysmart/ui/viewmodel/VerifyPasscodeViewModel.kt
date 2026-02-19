package net.metalbrain.paysmart.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import javax.inject.Inject

@HiltViewModel
class VerifyPasscodeViewModel @Inject constructor(
    private val repo: PasscodeRepository
) : ViewModel() {

    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    private val _passcode = MutableStateFlow("")
    val passcode: StateFlow<String> = _passcode

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _attempts = MutableStateFlow(0)
    val attempts: StateFlow<Int> = _attempts

    private val _biometricPrompt = MutableStateFlow(false)
    val biometricPrompt: StateFlow<Boolean> = _biometricPrompt

    private val _verified = MutableStateFlow(false)
    val verified: StateFlow<Boolean> = _verified

    private val maxAttempts = 5
    private var failedAttempts = 0
    private var isLocked = false

    private val _isLockedOut = MutableStateFlow(false)
    val isLockedOut: StateFlow<Boolean> = _isLockedOut

    private val lastShakeTrigger = mutableLongStateOf(0L)
    val shakeTrigger: State<Long> = lastShakeTrigger

    fun appendDigit(digit: Int) {
        if (_passcode.value.length >= 6 || isLocked) return
        _passcode.value += digit.toString()

        if (_passcode.value.length == 6) {
            verifyPasscode()
        }
    }

    fun appendDigit(digit: Char) {
        if (_passcode.value.length >= 6) return
        _passcode.value += digit
        _error.value = null

        if (_passcode.value.length == 6) {
            verifyPasscode()
        }
    }

    fun removeLastDigit() {
        _passcode.value = _passcode.value.dropLast(1)
        _error.value = null
    }

    private fun verifyPasscode() {
        val input = _passcode.value
        viewModelScope.launch {
            val success = repo.verify(input)
            if (success) {
                // Reset error state and attempts
                _error.value = null
                _attempts.value = 0
                _passcode.value = ""
                _verified.value = true
                // You can emit success state or navigate
            } else {
                _error.value = "Incorrect passcode"
                _passcode.value = ""
                _attempts.value += 1
                if (_attempts.value >= MAX_ATTEMPTS) {
                    _biometricPrompt.value = true
                }
            }
        }
    }

    private suspend fun validatePasscode() {
        val input = _passcode.value
        val isValid = repo.verify(input)

        if (isValid) {
            reset()
        } else {
            failedAttempts++
            _error.value = "Incorrect passcode"

            // 👋 Trigger shake
            lastShakeTrigger.longValue = System.currentTimeMillis()

            // 🔁 After 3 failures, fallback to biometrics
            if (failedAttempts == 3) {
                _biometricPrompt.value = true
            }

            // ⛔ Lockout after 5
            if (failedAttempts >= maxAttempts) {
                isLocked = true
                _isLockedOut.value = true
                viewModelScope.launch {
                    delay(30_000) // 30 seconds lockout
                    isLocked = false
                    _isLockedOut.value = false
                    failedAttempts = 0
                }
            }

            // Clear passcode field
            _passcode.value = ""
        }
    }

    private fun reset() {
        _error.value = null
        _passcode.value = ""
        failedAttempts = 0
        isLocked = false
        _isLockedOut.value = false
    }

    fun onBiometricDismissed() {
        _biometricPrompt.value = false
    }

    fun onVerifiedConsumed() {
        _verified.value = false
    }
}

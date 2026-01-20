package net.metalbrain.paysmart.ui.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.BiometricHelper
import net.metalbrain.paysmart.core.auth.BiometricPolicyHandler
import net.metalbrain.paysmart.utils.FailureCounter
import javax.inject.Inject
import androidx.fragment.app.FragmentActivity

@HiltViewModel
class BiometricOptInViewModel @Inject constructor(
    private val biometricPolicyHandler: BiometricPolicyHandler
) : ViewModel() {

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable: StateFlow<Boolean> = _biometricAvailable

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _lockedOut = MutableStateFlow(false)
    val lockedOut: StateFlow<Boolean> = _lockedOut

    private val failureCounter = FailureCounter(3)

    fun checkBiometricSupport(context: Context) {
        _biometricAvailable.value = BiometricHelper.isBiometricAvailable(context)
    }

    fun enableBiometric(
        activity: FragmentActivity,
        idToken: String
    ) {
        if (!_biometricAvailable.value) {
            _errorMessage.value = "Biometric not available on this device"
            return
        }

        _loading.value = true
        _errorMessage.value = null
        _lockedOut.value = false

        BiometricHelper.showPrompt(
            activity = activity,
            title = "Face ID or Fingerprint",
            subtitle = "Verify to enable biometric security",
            onSuccess = {
                viewModelScope.launch {
                    val success = biometricPolicyHandler.setBiometricEnabled(idToken)
                    _biometricEnabled.value = success
                    _loading.value = false
                    if (!success) {
                        _errorMessage.value = "Failed to enable biometric on server"
                    }
                }
            },
            onError = { err ->
                _loading.value = false
                _errorMessage.value = err
            },
            onFailureLimitReached = {
                _lockedOut.value = true
                _loading.value = false
                _errorMessage.value = "Too many attempts"
            },
            failureCounter = failureCounter
        )
    }

    fun skipBiometricSetup() {
        _biometricEnabled.value = false
        _errorMessage.value = null
        _loading.value = false
    }

    fun resetErrors() {
        _errorMessage.value = null
        _lockedOut.value = false
    }
}

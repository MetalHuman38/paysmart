package net.metalbrain.paysmart.ui.viewmodel



import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.BiometricHelper
import net.metalbrain.paysmart.utils.FailureCounter
import javax.inject.Inject
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.data.repository.BiometricRepository
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.security.SecurityPolicyEngine

@HiltViewModel
class BiometricOptInViewModel @Inject constructor(
    private val biometricRepository: BiometricRepository,
    securityPreference: SecurityPreference,
    private val policyEngine: SecurityPolicyEngine,
) : ViewModel() {
    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable = _biometricAvailable.asStateFlow()

    val biometricCompleted: StateFlow<Boolean> =
        securityPreference.localSecurityStateFlow
            .map { it.biometricsRequired && it.biometricsEnabled }
            .stateIn( viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _lockedOut = MutableStateFlow(false)
    val lockedOut = _lockedOut.asStateFlow()

    private val failureCounter = FailureCounter(3)

    fun checkBiometricSupport(context: Context) {
        _biometricAvailable.value = BiometricHelper.isBiometricAvailable(context)
    }

    fun enableBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit
    ) {
        if (!_biometricAvailable.value) {
            _errorMessage.value = "Biometric not available"
            return
        }

        _loading.value = true
        _errorMessage.value = null
        _lockedOut.value = false

        BiometricHelper.showPrompt(
            activity = activity,
            title = "Enable Face ID or Fingerprint",
            subtitle = "Verify your identity to enable biometric security",
            onSuccess = {
                viewModelScope.launch {
                    val biometricAlreadyEnabled = biometricRepository.isBiometricSetupComplete()
                    if (biometricAlreadyEnabled) {
                        _loading.value = false
                        onSuccess()
                        return@launch
                    }
                    // 2️⃣ Attempt secure server sync in background
                    val idToken = FirebaseAuth.getInstance()
                        .currentUser?.getIdToken(false)?.await()?.token
                    if (idToken.isNullOrBlank()) {
                        _errorMessage.value = "Missing auth token"
                        return@launch
                    }

                    val success = biometricRepository.enableBiometric(idToken)
                    _loading.value = false
                    if (success) {
                        // ✅ Notify UI that biometric setup was successful
                        onSuccess()
                    } else {
                        _errorMessage.value = "Failed to enable biometric security"
                    }
                }
            },
            onError = {
                _errorMessage.value = it
                _loading.value = false
            },
            onFailureLimitReached = {
                _lockedOut.value = true
                _errorMessage.value = "Too many attempts"
                _loading.value = false
            },
            failureCounter = failureCounter
        )
    }

    fun authenticateBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit
    ) {
        // Reset UI state
        _errorMessage.value = null
        _lockedOut.value = false
        _loading.value = true

        if (!_biometricAvailable.value) {
            _errorMessage.value = "Biometric not available"
            _loading.value = false
            return
        }

        // ✅ Otherwise → this is a SESSION UNLOCK (not opt-in)
        viewModelScope.launch {
            policyEngine.promptBiometric(
                activity = activity,
                onSuccess = {
                    _loading.value = false
                    onSuccess()
                },
                onFailure = {
                    _errorMessage.value = "Authentication failed"
                    _loading.value = false
                }
            )
        }
    }



    fun resetErrors() {
        _errorMessage.value = null
        _lockedOut.value = false
    }
}

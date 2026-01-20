package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.PassCodePolicyHandler
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val useCase: SecurityUseCase,
    private val policyHandler: PassCodePolicyHandler
) : ViewModel() {

    val securitySettings: StateFlow<SecuritySettings?> =
        useCase.settingsFlow

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    init {
        viewModelScope.launch {
            useCase.fetchCloudSettings()
            checkIfLocked()
        }
    }

    suspend fun fetchSecuritySettings() {
        useCase.fetchCloudSettings()
        checkIfLocked()
    }

    suspend fun markPasscodeEnabledOnServer() {
        policyHandler.setPassCodeEnabled(
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                ?: ""
        )
    }

    suspend fun getPasscodeEnabledOnServer(): Boolean {
        return policyHandler.getPasswordEnabled(
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                ?: ""
        )
    }


    suspend fun checkIfLocked() {
        _isLocked.value = useCase.isLocked()
    }

    suspend fun unlockSession() {
        useCase.unlockSession()
        _isLocked.value = false
    }

    suspend fun verify(passcode: String): Boolean =
        useCase.verifyPasscode(passcode)

    suspend fun hasPasscode(): Boolean =
        useCase.hasPasscode()

    suspend fun clearPasscode() =
        useCase.clearPasscode()
}

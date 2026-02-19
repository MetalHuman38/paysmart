package net.metalbrain.paysmart.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.state.SecureAppScreen
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase
import javax.inject.Inject

@HiltViewModel
class SecureAppViewModel @Inject constructor(
    application: Application,
    useCase: SecurityUseCase,
) : AndroidViewModel(application) {

    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    private val _activity = MutableStateFlow<FragmentActivity?>(null)
    val activity: StateFlow<FragmentActivity?> = _activity.asStateFlow()

    private val _uiState = MutableStateFlow<SecureAppScreen>(SecureAppScreen.AppContent)
    val uiState: StateFlow<SecureAppScreen> = _uiState.asStateFlow()

    val localSecuritySettings: StateFlow<LocalSecuritySettingsModel?> =
        useCase.localSettingsFlow

    init {
        fetchIdToken()
    }

    private fun fetchIdToken() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(false)?.await()?.token
            _idToken.value = token
        }
    }

    fun setActivity(context: Activity?) {
        if (context is FragmentActivity) {
            _activity.value = context
        }
    }

    fun evaluateSecurityState() {
        viewModelScope.launch {
            val settings = localSecuritySettings.value
                ?: LocalSecuritySettingsModel()
            _uiState.value = when {
                settings.biometricsRequired && !settings.biometricsEnabled ->
                    SecureAppScreen.RequireBiometricOptIn

                !settings.passcodeEnabled ->
                    SecureAppScreen.RequirePasscodeSetup
                
                settings.biometricsEnabled && settings.sessionLocked ->
                    SecureAppScreen.RequireBiometricUnlock

                // ✅ Everything is good
                else -> SecureAppScreen.AppContent
            }
        }
    }

    fun clearActivity() {
        _activity.value = null
    }
}

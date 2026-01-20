package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import net.metalbrain.paysmart.data.repository.SecurityCloudRepository
import net.metalbrain.paysmart.domain.model.SecuritySettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.SecurityRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.asOnboardingState
import net.metalbrain.paysmart.domain.state.OnboardingState
import net.metalbrain.paysmart.domain.state.UserUiState
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val securitySettings = MutableStateFlow<SecuritySettings?>(null)

    val onboardingState: StateFlow<OnboardingState> =
        securitySettings
            .map { it?.asOnboardingState() ?: OnboardingState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OnboardingState()
            )

    init {
        observeAuthChanges()
    }

    private fun observeAuthChanges() {
        viewModelScope.launch {
            authRepository.authChanges.collectLatest { isLoggedIn ->
                if (!isLoggedIn) {
                    _uiState.value = UserUiState.Unauthenticated
                    return@collectLatest
                }

                val user = authRepository.currentUser
                val uid = user?.uid

                if (uid.isNullOrBlank()) {
                    _uiState.value = UserUiState.Unauthenticated
                    return@collectLatest
                }

                try {
                    userProfileRepository.watchByUid(uid).collectLatest { profile ->
                        if (profile == null || profile.uid.isBlank()) {
                            _uiState.value = UserUiState.Unauthenticated
                        } else {
                            loadSecuritySettings()
                            _uiState.value = UserUiState.ProfileLoaded(profile)
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = UserUiState.Error(
                        e.message ?: "Failed to load user profile"
                    )
                }
            }
        }
    }

    private suspend fun loadSecuritySettings() {
        securityRepository
            .getSettings()
            .onSuccess { securitySettings.value = it }
            .onFailure {
                // optional: log or fallback
                Log.e("UserViewModel", "Failed to load security settings", it)
            }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

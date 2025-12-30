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
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.asOnboardingState
import net.metalbrain.paysmart.domain.state.OnboardingState
import net.metalbrain.paysmart.domain.state.UserUiState
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val securityRepo: SecurityCloudRepository,
    private val passcodeRepo: PasscodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    private val securitySettings = MutableStateFlow<SecuritySettings?>(null)

    val onboardingState: StateFlow<OnboardingState> =
        securitySettings
            .map { it?.asOnboardingState() ?: OnboardingState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
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
                Log.d("UserViewModel", "Attempting create for user: ${user?.uid}")
                val uid = user?.uid

                // 🔐 Defensive check — user could be a zombie
                if (uid.isNullOrBlank()) {
                    _uiState.value = UserUiState.Unauthenticated
                    return@collectLatest
                }

                try {
                    userProfileRepository.watchByUid(uid).collectLatest { profile ->
                        if (profile == null || profile.uid.isBlank()) {
                            Log.d("UserViewModel", "Profile snapshot: $profile")
                            Log.w("UserViewModel", "No valid profile found., uid: $uid")
                            _uiState.value = UserUiState.Unauthenticated
                        } else {
                            val settings = securityRepo.getSettings(uid)
                            securitySettings.value = settings
                            _uiState.value = UserUiState.ProfileLoaded(profile)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error watching user profile", e)
                    _uiState.value = UserUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }


    suspend fun getSecuritySettings(): SecuritySettings? {
        val user = authRepository.currentUser ?: return null
        return securityRepo.getSettings(user.uid)
    }

    fun shouldLock(lockAfterMinutes: Int?): Boolean {
        val minutes = lockAfterMinutes ?: return false
        return passcodeRepo.isLockRequired(minutes)
    }

    fun showPasscodePrompt(): Boolean {
        return passcodeRepo.promptForPasscode()
    }

    fun hasLocalPasscode(): Boolean {
        return passcodeRepo.hasPasscode()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

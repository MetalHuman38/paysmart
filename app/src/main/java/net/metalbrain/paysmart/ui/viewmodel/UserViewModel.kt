package net.metalbrain.paysmart.ui.viewmodel

import net.metalbrain.paysmart.data.repository.SecurityCloudRepository
import net.metalbrain.paysmart.domain.model.SecuritySettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.PasscodeRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
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

                // 🔐 Defensive check — user could be a zombie
                if (uid.isNullOrBlank()) {
                    _uiState.value = UserUiState.AuthenticatedButNoProfile
                    return@collectLatest
                }

                try {
                    userProfileRepository.watchByUid(uid).collectLatest { profile ->
                        if (profile != null) {
                            _uiState.value = UserUiState.ProfileLoaded(profile)
                        } else {
                            _uiState.value = UserUiState.AuthenticatedButNoProfile
                        }
                    }
                } catch (e: Exception) {
                    // 🛡️ Handle Firestore failures safely (offline, etc.)
                    if(e is FirebaseFirestoreException) {
                        _uiState.value = UserUiState.AuthenticatedButNoProfile
                    }
                    _uiState.value = UserUiState.AuthenticatedButNoProfile
                }
            }
        }
    }


    suspend fun getSecuritySettings(): SecuritySettings? {
        val user = authRepository.currentUser ?: return null
        return securityRepo.getSettings(user.uid)
    }

    fun shouldLock(lockAfterMinutes: Int?): Boolean {
        val minutes = lockAfterMinutes ?: return false // or provide a default
        return passcodeRepo.isLockRequired(minutes)
    }

    fun showPasscodePrompt(): Boolean {
        return passcodeRepo.promptForPasscode()
    }

    fun hasLocalPasscode(): Boolean {
        return passcodeRepo.hasPasscode()
    }
}

package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.UserProfileCacheRepository
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.model.asOnboardingState
import net.metalbrain.paysmart.domain.state.OnboardingState
import net.metalbrain.paysmart.domain.state.UserUiState
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    userProfileRepository: UserProfileRepository,
    private val profileCacheRepository: UserProfileCacheRepository,
) : ViewModel() {

    // By using `stateIn` with `SharingStarted.WhileSubscribed`, we ensure that the
    // upstream flow from `watchByUid` (and its Firestore listener) is only active when
    // there's a UI component actively collecting `uiState`. This prevents the
    // "ENHANCE_YOUR_CALM" error from Firestore.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UserUiState> = authRepository.authChanges
        .flatMapLatest { isLoggedIn ->
            if (!isLoggedIn) {
                flowOf<UserUiState>(UserUiState.Unauthenticated)
            } else {
                val authUser = authRepository.currentUser
                val uid = authUser?.uid
                if (uid == null) {
                    flowOf<UserUiState>(UserUiState.Unauthenticated)
                } else {
                    flow<UserUiState> {
                        profileCacheRepository.ensureSeed(
                            uid = uid,
                            displayName = authUser.displayName,
                            email = authUser.email,
                            phoneNumber = authUser.phoneNumber,
                            photoURL = authUser.photoUrl?.toString()
                        )

                        val remoteFlow = userProfileRepository.watchByUid(uid)
                            .onEach { remote ->
                                if (remote != null) {
                                    profileCacheRepository.upsertFromRemote(remote)
                                }
                            }

                        combine(
                            profileCacheRepository.observeByUid(uid),
                            remoteFlow
                        ) { local, remote ->
                            remote ?: local
                        }.map { profile ->
                            profile?.let { UserUiState.ProfileLoaded(it) } ?: UserUiState.Loading
                        }.collect { next ->
                            emit(next)
                        }
                    }
                }
            }
        }
        .catch { e ->
            Log.e("UserViewModel", "Error observing auth/profile", e)
            emit(UserUiState.Error(e.message ?: "Failed to load user profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserUiState.Loading
        )

    private val _securitySettings = MutableStateFlow<SecuritySettingsModel?>(null)
    val securitySettings: StateFlow<SecuritySettingsModel?> = _securitySettings.asStateFlow()

    val onboardingState: StateFlow<OnboardingState> =
        _securitySettings
            .map { it?.asOnboardingState() ?: OnboardingState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OnboardingState()
            )

    init {
        // Trigger loading security settings only when the user logs in.
        viewModelScope.launch {
            uiState
                .map { if (it is UserUiState.ProfileLoaded) it.user else null }
                .filterNotNull()
                .map { it.uid }
                .distinctUntilChanged() // Ensures this runs only once per user
                .collectLatest { _ ->
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

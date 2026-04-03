package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.profile.data.repository.ProfileRepository
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileAccountState
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft

@HiltViewModel
class ProfileStateViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileAccountState> =
        profileRepository.observeProfileState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ProfileAccountState()
            )

    fun cacheProfileDraft(draft: ProfileDetailsDraft) {
        viewModelScope.launch {
            profileRepository.saveProfileDraft(draft)
        }
    }
}

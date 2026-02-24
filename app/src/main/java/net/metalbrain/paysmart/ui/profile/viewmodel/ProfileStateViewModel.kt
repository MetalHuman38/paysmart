package net.metalbrain.paysmart.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.domain.model.ProfileDetailsDraft
import net.metalbrain.paysmart.ui.profile.data.repository.ProfileRepository
import net.metalbrain.paysmart.ui.profile.state.ProfileAccountState

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

package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.profile.data.repository.ProfileRepository

@HiltViewModel
class ProfilePhotoViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilePhotoUiState())
    val uiState: StateFlow<ProfilePhotoUiState> = _uiState.asStateFlow()

    fun savePresetAvatar(token: String) {
        save { profileRepository.savePresetAvatar(token) }
    }

    fun uploadProfilePhoto(fileName: String, mimeType: String, bytes: ByteArray) {
        save { profileRepository.uploadProfilePhoto(fileName, mimeType, bytes) }
    }

    fun removeProfilePhoto() {
        save { profileRepository.removeProfilePhoto() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun save(block: suspend () -> Result<Unit>) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            block()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = null,
                            completedAt = System.currentTimeMillis()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.localizedMessage ?: "Unable to update photo right now."
                        )
                    }
                }
        }
    }
}

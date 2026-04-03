package net.metalbrain.paysmart.core.features.account.profile.viewmodel

data class ProfilePhotoUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val completedAt: Long? = null
)

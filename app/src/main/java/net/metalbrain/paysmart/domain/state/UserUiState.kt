package net.metalbrain.paysmart.domain.state

import net.metalbrain.paysmart.domain.model.AuthUserModel

sealed interface UserUiState {
    object Loading : UserUiState
    object Unauthenticated : UserUiState
    object AuthenticatedButNoProfile : UserUiState
    data class ProfileLoaded(val user: AuthUserModel) : UserUiState
    data class Error(val message: String) : UserUiState
}

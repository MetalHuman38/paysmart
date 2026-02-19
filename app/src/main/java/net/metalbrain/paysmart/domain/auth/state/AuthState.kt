package net.metalbrain.paysmart.domain.auth.state


sealed interface AuthState {
    data object Unauthenticated : AuthState

    data object Loading : AuthState

    data class Authenticated(
        val uid: String
    ) : AuthState
}

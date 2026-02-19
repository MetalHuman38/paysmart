package net.metalbrain.paysmart.domain.auth.state


sealed interface PostAuthState {

    data object Loading : PostAuthState
    data object Unauthenticated : PostAuthState

    data object RequireAccountProtection : PostAuthState

    data object RequireEmailVerification : PostAuthState

    data object Locked : PostAuthState

    data object Ready : PostAuthState
}

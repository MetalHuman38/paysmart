package net.metalbrain.paysmart.domain.auth.state

sealed interface PostAuthState {

    data object Loading : PostAuthState
    data object Unauthenticated : PostAuthState

    data object RequireRecoveryMethod : PostAuthState
    data object RequireRecoveryPassword : PostAuthState
    data object RequirePasswordRecovery : PostAuthState

    data object Locked : PostAuthState

    data object Ready : PostAuthState
}

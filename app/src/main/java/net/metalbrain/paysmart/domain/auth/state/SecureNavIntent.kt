package net.metalbrain.paysmart.domain.auth.state

sealed interface SecureNavIntent {
    data object ToStartup : SecureNavIntent
    data object ToAccountProtection : SecureNavIntent
    data object ToEmailVerification : SecureNavIntent
    data object RequireSessionUnlock: SecureNavIntent

    data object None : SecureNavIntent
}

package net.metalbrain.paysmart.domain.auth.state

sealed interface SecureNavIntent {
    data object ToStartup : SecureNavIntent
    data object ToRecoveryMethod : SecureNavIntent
    data object ToRecoveryPassword : SecureNavIntent
    data object ToPasswordRecovery : SecureNavIntent
    data object RequireSessionUnlock : SecureNavIntent

    data object None : SecureNavIntent
}

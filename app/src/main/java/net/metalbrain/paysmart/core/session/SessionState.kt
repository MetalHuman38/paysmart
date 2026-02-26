package net.metalbrain.paysmart.core.session


sealed class SessionState {
    object Loading : SessionState()
    object Locked : SessionState()
    object Unlocked : SessionState()

    data class Error(val message: String) : SessionState()
}

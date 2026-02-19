package net.metalbrain.paysmart.domain.auth

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState


interface UserManager {
    val uid: String

    val authState: Flow<AuthState>

    fun signOut()
}

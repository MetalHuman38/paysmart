package net.metalbrain.paysmart.domain.auth

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.auth.state.AuthState


/**
 * Manages the authentication state and profile information for the current user.
 *
 * This interface provides access to the user's unique identifier and reactive updates
 * regarding their authentication status, as well as the ability to terminate the session.
 */
interface UserManager {
    val uid: String

    val authState: Flow<AuthState>

    fun signOut()
}

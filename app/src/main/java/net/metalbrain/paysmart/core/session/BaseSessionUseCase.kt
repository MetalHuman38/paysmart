package net.metalbrain.paysmart.core.session


import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.AuthSession
import javax.inject.Inject

class BaseSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * Returns the current authenticated session, or throws if not available.
     */
    suspend fun currentSession(): AuthSession {
        return authRepository.getCurrentSessionOrThrow()
    }

    /**
     * Returns the current userId and idToken as Pair.
     */
    suspend fun currentUserAndToken(): Pair<String, String> {
        val session = currentSession()
        return session.user.uid to session.idToken
    }

    /**
     * Returns only the user ID if authenticated.
     */
    suspend fun currentUserId(): String = currentSession().user.uid

    /**
     * Returns only the Firebase ID token.
     */
    suspend fun currentIdToken(): String = currentSession().idToken
}

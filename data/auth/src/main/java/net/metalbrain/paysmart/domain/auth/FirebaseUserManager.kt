package net.metalbrain.paysmart.domain.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.metalbrain.paysmart.domain.auth.state.AuthState
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Firebase-based implementation of [UserManager] that manages user authentication state using [FirebaseAuth].
 *
 * This class provides a reactive stream of the user's authentication status via a [Flow],
 * exposes the current user's unique identifier, and handles the sign-out process.
 *
 * @property auth The [FirebaseAuth] instance used to interact with Firebase Authentication services.
 */
class FirebaseUserManager @Inject constructor(
    private val auth: FirebaseAuth,
) : UserManager {

    override val uid: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("No authenticated user")

    override val authState: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user == null) {
                trySend(AuthState.Unauthenticated)
            } else {
                trySend(AuthState.Authenticated(user.uid))
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()


    override fun signOut() {
        auth.signOut()
    }
}

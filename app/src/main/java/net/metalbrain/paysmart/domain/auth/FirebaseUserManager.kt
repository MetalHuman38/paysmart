package net.metalbrain.paysmart.domain.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.metalbrain.paysmart.domain.auth.state.AuthState
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged

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

package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authChanges: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun signInAnonymously(): AuthResult = firebaseAuth.signInAnonymously().await()

    override suspend fun signInWithCredential(credential: AuthCredential): AuthResult =
        firebaseAuth.signInWithCredential(credential).await()

    override suspend fun linkWithCredential(credential: AuthCredential) =
        firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
            ?: throw IllegalStateException("No current user to link credentials")

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

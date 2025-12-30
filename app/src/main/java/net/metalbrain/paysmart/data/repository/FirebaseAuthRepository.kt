package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    override suspend fun isPhoneUnique(phone: String): Boolean {
        return try {
            FirebaseFunctions.getInstance()
                .getHttpsCallable("checkPhoneUnique")
                .call(mapOf("phoneNumber" to phone))
                .await()
            true
        } catch (e: FirebaseFunctionsException) {
            e.code != FirebaseFunctionsException.Code.ALREADY_EXISTS
        } catch (e: Exception) {
            // Optional: log or rethrow
            e.printStackTrace()
            false
        }
    }



    override suspend fun checkPhoneAlreadyRegistered(phone: String): Boolean {
        val snapshot = firestore.collection("users")
            .whereEqualTo("phoneNumber", phone)
            .limit(1)
            .get()
            .await()

        return !snapshot.isEmpty
    }

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

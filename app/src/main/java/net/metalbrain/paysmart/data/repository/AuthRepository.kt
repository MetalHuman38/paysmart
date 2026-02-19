package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authChanges: Flow<Boolean>
    val isLoggedIn: Boolean
    val currentUser: FirebaseUser?

    suspend fun getCurrentSession(): AuthSession?

    suspend fun getCurrentSessionOrThrow(): AuthSession

    suspend fun checkPhoneAlreadyRegistered(phone: String): Boolean

    suspend fun isPhoneUnique(phone: String): Boolean

    suspend fun signInAnonymously(): AuthResult
    suspend fun signInWithCredential(credential: AuthCredential): AuthResult
    suspend fun linkWithCredential(credential: AuthCredential): AuthResult

    suspend fun signOut()
}

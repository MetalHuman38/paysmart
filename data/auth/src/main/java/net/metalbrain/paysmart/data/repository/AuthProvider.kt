package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.AuthCredential

/**
 * Represents a provider capable of supplying an [AuthCredential] for authentication.
 */
interface AuthProvider {
    val providerId: String
    suspend fun getCredential(): AuthCredential
}

package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.AuthCredential

interface SignInProvider {
    val providerId: String
    suspend fun getCredential(): AuthCredential
}

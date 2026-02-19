package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.FirebaseUser

data class AuthSession(
    val user: FirebaseUser,
    val idToken: String
)

package net.metalbrain.paysmart.data.repository

import com.google.firebase.auth.FirebaseUser

/**
 * Represents an authenticated user session, containing the Firebase user details
 * and the associated ID token for authentication with backend services.
 *
 * @property user The [FirebaseUser] instance containing profile information.
 * @property idToken The current Firebase Authentication ID token (JWT) used for authorized requests.
 */
data class AuthSession(
    val user: FirebaseUser,
    val idToken: String
)

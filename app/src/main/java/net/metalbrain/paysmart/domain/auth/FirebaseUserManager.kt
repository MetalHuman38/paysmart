package net.metalbrain.paysmart.domain.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseUserManager @Inject constructor(
    private val auth: FirebaseAuth
) : UserManager {
    override val uid: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("No authenticated user")
}

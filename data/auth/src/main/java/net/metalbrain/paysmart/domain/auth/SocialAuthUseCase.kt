package net.metalbrain.paysmart.domain.auth

import android.app.Activity
import com.google.firebase.auth.AuthCredential

interface SocialAuthUseCase {
    suspend fun signInWithGoogle(credential: AuthCredential): Result<Unit>
    suspend fun signInWithFacebook(activity: Activity): Result<Unit>
    suspend fun linkCredential(credential: AuthCredential): Result<Unit>
}

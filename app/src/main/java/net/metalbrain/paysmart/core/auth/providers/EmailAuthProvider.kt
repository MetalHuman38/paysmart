package net.metalbrain.paysmart.core.auth.providers

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object EmailLinkAuthProvider {

    private const val CONTINUE_URL = "https://pay-smart.net/verify"

    fun buildActionCodeSettings(context: Context): ActionCodeSettings {
        return ActionCodeSettings.newBuilder()
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                context.packageName,
                true, /* installIfNotAvailable */
                null /* minimumVersion */
            )
            .setUrl(CONTINUE_URL)
            .build()
    }

    suspend fun sendEmailLink(
        context: Context,
        email: String
    ) {
        val actionCodeSettings = buildActionCodeSettings(context)
        FirebaseAuth.getInstance().sendSignInLinkToEmail(email, actionCodeSettings).await()
    }

    suspend fun signInWithEmailLink(
        email: String,
        intent: Intent
    ): FirebaseUser {
        val auth = FirebaseAuth.getInstance()
        val emailLink = intent.data?.toString() ?: throw IllegalArgumentException("Invalid link")

        if (!auth.isSignInWithEmailLink(emailLink)) {
            throw IllegalArgumentException("Not a valid email sign-in link")
        }

        val result = auth.signInWithEmailLink(email, emailLink).await()
        return result.user ?: throw Exception("User not found after sign-in")
    }
}

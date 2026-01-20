package net.metalbrain.paysmart.domain.usecase

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import net.metalbrain.paysmart.core.auth.providers.EmailLinkAuthProvider
import javax.inject.Inject

class EmailLinkUseCase @Inject constructor() {

    suspend fun sendMagicLink(context: Context, email: String) {
        EmailLinkAuthProvider.sendEmailLink(context, email)
    }

    suspend fun handleEmailLinkIntent(email: String, intent: Intent): FirebaseUser {
        return EmailLinkAuthProvider.signInWithEmailLink(email, intent)
    }
}

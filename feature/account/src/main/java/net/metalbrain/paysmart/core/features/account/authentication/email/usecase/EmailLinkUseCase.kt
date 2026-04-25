package net.metalbrain.paysmart.core.features.account.authentication.email.usecase

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import net.metalbrain.paysmart.core.features.account.authentication.email.provider.EmailLinkAuthProvider
import javax.inject.Inject

class EmailLinkUseCase @Inject constructor() {

    suspend fun sendMagicLink(context: Context, email: String) {
        EmailLinkAuthProvider.sendEmailLink(context, email)
    }

    suspend fun handleEmailLinkIntent(email: String, intent: Intent): FirebaseUser {
        return EmailLinkAuthProvider.signInWithEmailLink(email, intent)
    }
}

package net.metalbrain.paysmart.email


import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import net.metalbrain.paysmart.core.auth.providers.EmailLinkAuthProvider
import javax.inject.Inject


class EmailAuthHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val emailDraftStore: EmailDraftStore,
    private val firebaseAuth: FirebaseAuth,
    private val coroutineScope: CoroutineScope
) : EmailVerifier {

    private var onLinkSent: (() -> Unit)? = null
    private var onVerified: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    override suspend fun start(email: String) {
        try {
            EmailLinkAuthProvider.sendEmailLink(context, email)

            // Store email locally
            emailDraftStore.saveDraft(
                EmailDraft(email = email, verified = false)
            )

            onLinkSent?.invoke()
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override suspend fun checkStatus(): Boolean {
        val user = firebaseAuth.currentUser
        val draft = emailDraftStore.draft.first()

        val isVerified = user?.isEmailVerified == true || draft.verified

        if (isVerified) {
            // Mark locally
            emailDraftStore.saveDraft(
                EmailDraft(email = user?.email, verified = true)
            )
            onVerified?.invoke()
        }

        return isVerified
    }

    override fun setCallbacks(
        onLinkSent: (() -> Unit)?,
        onVerified: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        this.onLinkSent = onLinkSent
        this.onVerified = onVerified
        this.onError = onError
    }
}

package net.metalbrain.paysmart.ui.account.recovery.auth.gateway

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.ui.account.recovery.auth.ChangePhoneRecoveryAuthGateway
import net.metalbrain.paysmart.ui.account.recovery.auth.data.PhoneRecoverySession
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirebaseChangePhoneRecoveryAuthGateway @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ChangePhoneRecoveryAuthGateway {

    override fun hasAuthenticatedUser(): Boolean = firebaseAuth.currentUser != null

    override fun startPhoneVerification(
        activity: FragmentActivity,
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        resendToken?.let(optionsBuilder::setForceResendingToken)
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    override fun credentialFromCode(verificationId: String, code: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, code)
    }

    override suspend fun applyPhoneCredential(
        credential: PhoneAuthCredential,
        fallbackPhoneE164: String?
    ): PhoneRecoverySession {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("Session expired. Please sign in again.")

        user.updatePhoneNumber(credential).await()
        user.reload().await()

        val resolvedPhone = user.phoneNumber ?: fallbackPhoneE164
        if (resolvedPhone.isNullOrBlank()) {
            throw IllegalStateException("Phone number update completed but no phone number was returned.")
        }

        val idToken = user.getIdToken(true).await().token
            ?: throw IllegalStateException("Token missing")

        return PhoneRecoverySession(
            uid = user.uid,
            phoneNumber = resolvedPhone,
            idToken = idToken
        )
    }
}

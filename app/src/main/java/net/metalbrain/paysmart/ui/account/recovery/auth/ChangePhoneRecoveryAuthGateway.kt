package net.metalbrain.paysmart.ui.account.recovery.auth

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import net.metalbrain.paysmart.ui.account.recovery.auth.data.PhoneRecoverySession

interface ChangePhoneRecoveryAuthGateway {
    fun hasAuthenticatedUser(): Boolean

    fun startPhoneVerification(
        activity: FragmentActivity,
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    fun credentialFromCode(verificationId: String, code: String): PhoneAuthCredential

    suspend fun applyPhoneCredential(
        credential: PhoneAuthCredential,
        fallbackPhoneE164: String?
    ): PhoneRecoverySession
}

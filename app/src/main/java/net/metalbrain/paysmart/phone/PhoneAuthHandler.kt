package net.metalbrain.paysmart.phone

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PhoneAuthHandler(
    private val auth: FirebaseAuth,
    private val coroutineScope: CoroutineScope,
    private val phoneDraftState: MutableStateFlow<PhoneDraft>
) : PhoneVerifier {

    private var storedVerificationId: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var codeSentCallback: (() -> Unit)? = null
    private var onAutoVerified: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    override suspend fun start(e164: String, activity: Activity) {
        withContext(Dispatchers.Main) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(e164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(getVerificationCallbacks(e164))
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun submitOtp(code: String): Result<Unit> {
        val verificationId = phoneDraftState.value.verificationId
            ?: return Result.failure(IllegalStateException("No verification ID in state"))

        Log.d("PhoneAuth", "Using verification ID: $verificationId")

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return suspendCancellableCoroutine { cont ->
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    phoneDraftState.value = phoneDraftState.value.copy(verified = true)
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun resend(e164: String, activity: Activity): Result<Unit> {
        val currentE164 = phoneDraftState.value.e164
            ?: return Result.failure(IllegalStateException("E.164 phone number not available for resend"))

        val token = resendingToken ?: return Result.failure(IllegalStateException("No resending token"))

        withContext(Dispatchers.Main) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(e164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(getVerificationCallbacks(currentE164, isResend = true))
                .setForceResendingToken(token)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        return Result.success(Unit)
    }

    override fun cancel() {
        storedVerificationId = null
        resendingToken = null
    }

    override fun setCallbacks(
        onCodeSent: (() -> Unit)?,
        onAutoVerified: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        this.codeSentCallback = onCodeSent
        this.onAutoVerified = onAutoVerified
        this.onError = onError
    }

    private fun getVerificationCallbacks(e164: String, isResend: Boolean = false) =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                signInWithCredential(cred, autoVerified = true)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError?.invoke(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                phoneDraftState.value = phoneDraftState.value.copy(verificationId = verificationId)
                resendingToken = token
                if (isResend) {
                    phoneDraftState.value = phoneDraftState.value.copy(verificationId = verificationId)
                } else {
                    phoneDraftState.value =
                        phoneDraftState.value.copy(e164 = e164, verificationId = verificationId)
                }
                codeSentCallback?.invoke()
            }
        }

    private fun signInWithCredential(cred: PhoneAuthCredential, autoVerified: Boolean) {
        coroutineScope.launch {
            auth.signInWithCredential(cred)
                .addOnSuccessListener {
                    phoneDraftState.value = phoneDraftState.value.copy(verified = true)
                    if (autoVerified) {
                        onAutoVerified?.invoke()
                    }
                }
                .addOnFailureListener { onError?.invoke(it) }
        }
    }
}

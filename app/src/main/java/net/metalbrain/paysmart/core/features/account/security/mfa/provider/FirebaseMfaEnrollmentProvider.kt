package net.metalbrain.paysmart.core.features.account.security.mfa.provider

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorSession
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaEnrollmentChallenge
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaEnrollmentStatus
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseMfaEnrollmentProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : MfaEnrollmentProvider {
    private var multiFactorSession: MultiFactorSession? = null
    private var verificationId: String? = null
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var autoResolvedCredential: PhoneAuthCredential? = null
    private var destinationHint: String? = null

    override suspend fun loadStatus(): MfaEnrollmentStatus {
        val user = firebaseAuth.currentUser
        return MfaEnrollmentStatus(
            signedIn = user != null,
            emailVerified = user?.isEmailVerified == true,
            hasEnrolledFactor = user?.multiFactor
                ?.enrolledFactors
                ?.isNotEmpty() == true
        )
    }

    override suspend fun startSession(): Result<MfaEnrollmentChallenge> = runCatching {
        val user = requireCurrentUser()
        val phoneNumber = requirePhoneNumber(user)
        if (!user.isEmailVerified) {
            throw IllegalStateException("Verified email is required before enabling 2-step verification")
        }

        multiFactorSession = user.multiFactor.session.await()
        destinationHint = maskPhone(phoneNumber)
        MfaEnrollmentChallenge(destinationHint = destinationHint ?: maskPhone(phoneNumber))
    }

    override suspend fun sendVerificationCode(activity: Activity): Result<MfaEnrollmentChallenge> {
        return runCatching {
            val user = requireCurrentUser()
            val phoneNumber = requirePhoneNumber(user)
            val session = multiFactorSession ?: user.multiFactor.session.await().also {
                multiFactorSession = it
            }
            val hint = destinationHint ?: maskPhone(phoneNumber)
            destinationHint = hint
            autoResolvedCredential = null

            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { continuation ->
                    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            autoResolvedCredential = credential
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(exception)
                            }
                        }

                        override fun onCodeSent(
                            newVerificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            verificationId = newVerificationId
                            forceResendingToken = token
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }

                    val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(callbacks)
                        .setMultiFactorSession(session)
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }

            MfaEnrollmentChallenge(destinationHint = hint)
        }
    }

    override suspend fun resendVerificationCode(activity: Activity): Result<Unit> = runCatching {
        val user = requireCurrentUser()
        val phoneNumber = requirePhoneNumber(user)
        val session = multiFactorSession ?: user.multiFactor.session.await().also {
            multiFactorSession = it
        }
        val token = forceResendingToken
            ?: throw IllegalStateException("Cannot resend code before initial send")

        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        autoResolvedCredential = credential
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }

                    override fun onCodeSent(
                        newVerificationId: String,
                        newToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = newVerificationId
                        forceResendingToken = newToken
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .setMultiFactorSession(session)
                    .setForceResendingToken(token)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    override suspend fun verifyCodeAndEnroll(code: String, displayName: String?): Result<Unit> = runCatching {
        val user = requireCurrentUser()
        val credential = autoResolvedCredential ?: run {
            val verification = verificationId
                ?: throw IllegalStateException("No active MFA verification challenge")
            val normalizedCode = code.trim()
            if (normalizedCode.length < 6) {
                throw IllegalStateException("Enter the 6-digit verification code")
            }
            PhoneAuthProvider.getCredential(verification, normalizedCode)
        }
        val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
        user.multiFactor.enroll(assertion, displayName?.takeIf { it.isNotBlank() } ?: "PaySmart phone")
        user.reload().await()
        clearPendingSession()
    }

    override fun clearPendingSession() {
        multiFactorSession = null
        verificationId = null
        forceResendingToken = null
        autoResolvedCredential = null
    }

    private fun requireCurrentUser(): FirebaseUser {
        return firebaseAuth.currentUser
            ?: throw IllegalStateException("User must be signed in for MFA enrollment")
    }

    private fun requirePhoneNumber(user: FirebaseUser): String {
        return user.phoneNumber?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Phone number is required for phone-based MFA enrollment")
    }

    private fun maskPhone(phone: String): String {
        val trimmed = phone.trim()
        if (trimmed.length <= 4) return trimmed
        return "*".repeat(trimmed.length - 4) + trimmed.takeLast(4)
    }
}

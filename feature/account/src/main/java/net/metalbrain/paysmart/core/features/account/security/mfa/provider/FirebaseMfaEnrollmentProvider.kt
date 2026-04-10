package net.metalbrain.paysmart.core.features.account.security.mfa.provider

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
    private companion object {
        private const val PROVIDER_PHONE = "phone"
        private const val PROVIDER_ANONYMOUS = "anonymous"
        private const val PROVIDER_APPLE_GAME_CENTER = "gc.apple.com"
        private const val ERROR_UNSUPPORTED_FIRST_FACTOR = "ERROR_UNSUPPORTED_FIRST_FACTOR"
    }

    private var multiFactorSession: MultiFactorSession? = null
    private var verificationId: String? = null
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var autoResolvedCredential: PhoneAuthCredential? = null
    private var destinationHint: String? = null

    override suspend fun loadStatus(): MfaEnrollmentStatus {
        val user = firebaseAuth.currentUser
        val firstFactorBlockMessage = user?.let { resolveEnrollmentBlockMessage(it) }
        return MfaEnrollmentStatus(
            signedIn = user != null,
            emailVerified = user?.isEmailVerified == true,
            hasEnrolledFactor = user?.multiFactor
                ?.enrolledFactors
                ?.isNotEmpty() == true,
            supportsEnrollment = firstFactorBlockMessage == null,
            enrollmentBlockMessage = firstFactorBlockMessage,
            blockedActionLabel = user?.takeIf { firstFactorBlockMessage != null }?.let {
                resolveBlockedActionLabel(it)
            }
        )
    }

    override suspend fun startSession(): Result<MfaEnrollmentChallenge> {
        return try {
            val user = requireCurrentUser()
            val phoneNumber = requirePhoneNumber(user)
            requireSupportedFirstFactor(user)
            if (!user.isEmailVerified) {
                throw IllegalStateException("Verified email is required before enabling 2-step verification")
            }

            multiFactorSession = user.multiFactor.session.await()
            destinationHint = maskPhone(phoneNumber)
            Result.success(
                MfaEnrollmentChallenge(destinationHint = destinationHint ?: maskPhone(phoneNumber))
            )
        } catch (exception: Exception) {
            Result.failure(mapEnrollmentException(exception))
        }
    }

    override suspend fun sendVerificationCode(activity: Activity): Result<MfaEnrollmentChallenge> {
        return try {
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

            Result.success(MfaEnrollmentChallenge(destinationHint = hint))
        } catch (exception: Exception) {
            Result.failure(mapEnrollmentException(exception))
        }
    }

    override suspend fun resendVerificationCode(activity: Activity): Result<Unit> {
        return try {
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

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(mapEnrollmentException(exception))
        }
    }

    override suspend fun verifyCodeAndEnroll(code: String, displayName: String?): Result<Unit> {
        return try {
            val user = requireCurrentUser()

            if (autoResolvedCredential == null && verificationId == null) {
                user.reload().await()
                if (user.multiFactor.enrolledFactors.isNotEmpty()) {
                    clearPendingSession()
                    return Result.success(Unit)
                }
                return Result.failure(IllegalStateException("No active MFA verification challenge"))
            }

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
            user.multiFactor
                .enroll(assertion, displayName?.takeIf { it.isNotBlank() } ?: "PaySmart phone")
                .await()
            user.reload().await()
            clearPendingSession()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(mapEnrollmentException(exception))
        }
    }

    override fun clearPendingSession() {
        multiFactorSession = null
        verificationId = null
        forceResendingToken = null
        autoResolvedCredential = null
        destinationHint = null
    }

    private fun requireCurrentUser(): FirebaseUser {
        return firebaseAuth.currentUser
            ?: throw IllegalStateException("User must be signed in for MFA enrollment")
    }

    private fun requirePhoneNumber(user: FirebaseUser): String {
        return user.phoneNumber?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Phone number is required for phone-based MFA enrollment")
    }

    private suspend fun requireSupportedFirstFactor(user: FirebaseUser) {
        resolveEnrollmentBlockMessage(user)?.let { message ->
            throw IllegalStateException(message)
        }
    }

    private suspend fun resolveEnrollmentBlockMessage(user: FirebaseUser): String? {
        val signInProvider = user.getIdToken(false).await().signInProvider?.lowercase()
        return when (signInProvider) {
            PROVIDER_PHONE ->
                "2-step verification can't be enabled from a phone sign-in session. Sign in with email link or a linked provider, then try again."
            PROVIDER_ANONYMOUS ->
                "2-step verification isn't available for anonymous sessions. Sign in with a saved account first."
            PROVIDER_APPLE_GAME_CENTER ->
                "2-step verification isn't available from this Apple Game Center session. Sign in with another linked method and try again."
            else -> null
        }
    }

    private fun resolveBlockedActionLabel(user: FirebaseUser): String {
        val linkedProviderLabels = user.providerData
            .mapNotNull { profile ->
                when (profile.providerId.lowercase()) {
                    "google.com" -> "Google"
                    "facebook.com" -> "Facebook"
                    "password" -> "email"
                    "apple.com" -> "Apple"
                    else -> null
                }
            }
            .distinct()

        if (linkedProviderLabels.isEmpty()) {
            return "Sign out and use another sign-in method"
        }

        if (linkedProviderLabels.size == 1) {
            return if (linkedProviderLabels.first() == "email") {
                "Sign in with email link to continue"
            } else {
                "Use linked ${linkedProviderLabels.first()} to continue"
            }
        }

        val providersText = when (linkedProviderLabels.size) {
            2 -> "${linkedProviderLabels[0]} or ${linkedProviderLabels[1]}"
            else -> linkedProviderLabels.dropLast(1).joinToString(", ") +
                ", or ${linkedProviderLabels.last()}"
        }
        return "Use linked $providersText to continue"
    }

    private fun mapEnrollmentException(exception: Exception): Throwable {
        if (exception is FirebaseAuthException &&
            exception.errorCode == ERROR_UNSUPPORTED_FIRST_FACTOR
        ) {
            return IllegalStateException(
                "2-step verification can't be enabled from the current sign-in session. Sign in with email link or another linked provider, then try again.",
                exception
            )
        }
        return exception
    }

    private fun maskPhone(phone: String): String {
        val trimmed = phone.trim()
        if (trimmed.length <= 4) return trimmed
        return "*".repeat(trimmed.length - 4) + trimmed.takeLast(4)
    }
}

package net.metalbrain.paysmart.core.features.account.security.mfa.provider

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaSignInChallenge
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaSignInFactorOption
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseMfaSignInProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : MfaSignInProvider {

    private var pendingResolver: MultiFactorResolver? = null
    private var pendingHints: List<PhoneMultiFactorInfo> = emptyList()
    private var selectedFactorUid: String? = null
    private var verificationId: String? = null
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var autoResolvedCredential: PhoneAuthCredential? = null
    private var destinationHint: String? = null

    override fun beginChallenge(exception: FirebaseAuthMultiFactorException): Result<MfaSignInChallenge> {
        return runCatching {
            val resolver = exception.resolver
            val hints = resolver.hints
                .filterIsInstance<PhoneMultiFactorInfo>()
            val initialHint = hints.firstOrNull()
                ?: throw IllegalStateException(
                    "This account requires a second factor that PaySmart does not support yet."
                )

            pendingResolver = resolver
            pendingHints = hints
            selectedFactorUid = initialHint.uid
            verificationId = null
            forceResendingToken = null
            autoResolvedCredential = null
            destinationHint = resolveDestinationHint(initialHint)

            requirePendingChallenge()
        }
    }

    override fun getPendingChallenge(): MfaSignInChallenge? {
        return runCatching { requirePendingChallenge() }.getOrNull()
    }

    override fun selectFactor(factorUid: String): Result<MfaSignInChallenge> {
        return runCatching {
            val selectedHint = pendingHints.firstOrNull { it.uid == factorUid }
                ?: throw IllegalStateException("That verification method is no longer available.")
            selectedFactorUid = selectedHint.uid
            verificationId = null
            forceResendingToken = null
            autoResolvedCredential = null
            destinationHint = resolveDestinationHint(selectedHint)
            requirePendingChallenge()
        }
    }

    override suspend fun sendVerificationCode(activity: Activity): Result<MfaSignInChallenge> {
        return try {
            val resolver = requirePendingResolver()
            val hint = requireSelectedHint()
            val resolvedDestination = destinationHint ?: resolveDestinationHint(hint)
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
                        .setActivity(activity)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setCallbacks(callbacks)
                        .setMultiFactorHint(hint)
                        .setMultiFactorSession(resolver.session)
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }

            destinationHint = resolvedDestination
            Result.success(requirePendingChallenge())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun resendVerificationCode(activity: Activity): Result<Unit> {
        return try {
            val resolver = requirePendingResolver()
            val hint = requireSelectedHint()
            val token = forceResendingToken
                ?: throw IllegalStateException("Request a verification code before trying again.")

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
                        .setActivity(activity)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setCallbacks(callbacks)
                        .setMultiFactorHint(hint)
                        .setMultiFactorSession(resolver.session)
                        .setForceResendingToken(token)
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun verifyCodeAndSignIn(code: String): Result<Unit> {
        return runCatching {
            val resolver = requirePendingResolver()
            val credential = autoResolvedCredential ?: run {
                val activeVerificationId = verificationId
                    ?: throw IllegalStateException("Request a new verification code to continue.")
                val normalizedCode = code.trim()
                if (normalizedCode.length < 6) {
                    throw IllegalStateException("Enter the 6-digit verification code.")
                }
                PhoneAuthProvider.getCredential(activeVerificationId, normalizedCode)
            }
            val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
            resolver.resolveSignIn(assertion).await()
            firebaseAuth.currentUser?.getIdToken(true)?.await()
            clearPendingChallenge()
        }
    }

    override fun clearPendingChallenge() {
        pendingResolver = null
        pendingHints = emptyList()
        selectedFactorUid = null
        verificationId = null
        forceResendingToken = null
        autoResolvedCredential = null
        destinationHint = null
    }

    private fun requirePendingResolver(): MultiFactorResolver {
        return pendingResolver
            ?: throw IllegalStateException("This 2-step verification session expired. Start sign-in again.")
    }

    private fun requireSelectedHint(): PhoneMultiFactorInfo {
        val factorUid = selectedFactorUid
            ?: throw IllegalStateException("Choose a verification method to continue.")
        return pendingHints.firstOrNull { it.uid == factorUid }
            ?: throw IllegalStateException("That verification method is no longer available.")
    }

    private fun requirePendingChallenge(): MfaSignInChallenge {
        val hint = requireSelectedHint()
        val resolvedDestination = destinationHint ?: resolveDestinationHint(hint)
        destinationHint = resolvedDestination
        val factorOptions = pendingHints.map {
            MfaSignInFactorOption(
                factorUid = it.uid,
                displayName = it.displayName?.trim()?.takeIf(String::isNotBlank),
                destinationHint = resolveDestinationHint(it)
            )
        }
        return MfaSignInChallenge(
            factors = factorOptions,
            selectedFactorUid = hint.uid,
            destinationHint = resolvedDestination
        )
    }

    private fun resolveDestinationHint(hint: PhoneMultiFactorInfo): String {
        val phone = hint.phoneNumber.trim()
        if (phone.isBlank()) {
            return "your verified phone"
        }
        return if (phone.contains('*')) {
            phone
        } else {
            maskPhone(phone)
        }
    }

    private fun maskPhone(phone: String): String {
        val trimmed = phone.trim()
        if (trimmed.length <= 4) return trimmed
        return "*".repeat(trimmed.length - 4) + trimmed.takeLast(4)
    }
}

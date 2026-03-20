package net.metalbrain.paysmart.core.features.account.creation.phone.data

import android.app.Activity
import android.util.Log

/**
 * Preview bridge for Firebase Phone Number Verification (PNV).
 *
 * Current behavior delegates to legacy OTP flow until Firebase's preview API
 * and server verification contract are declared stable for production use.
 * Once the real PNV SDK is enabled, the post-verification user finalization
 * should still terminate in the same server-owned verified-phone upsert flow.
 */
class PnvPreviewPhoneVerifier(
    private val legacyVerifier: PhoneVerifier
) : PhoneVerifier {

    private companion object {
        private const val TAG = "PnvPreviewPhoneVerifier"
    }

    fun isRuntimeAvailable(): Boolean {
        // Keep this as a lightweight runtime probe so we can toggle without compile-time coupling.
        return runCatching {
            Class.forName("com.google.firebase.pnv.FirebasePhoneNumberVerification")
        }.isSuccess
    }

    override suspend fun start(e164: String, activity: Activity) {
        Log.i(
            TAG,
            "PNV preview route selected; delegating to legacy OTP pipeline until stable Firebase PNV API is enabled."
        )
        legacyVerifier.start(e164, activity)
    }

    override suspend fun submitOtp(code: String): Result<Unit> {
        return legacyVerifier.submitOtp(code)
    }

    override suspend fun resend(e164: String, activity: Activity): Result<Unit> {
        return legacyVerifier.resend(e164, activity)
    }

    override fun getVerificationId(): String? {
        return legacyVerifier.getVerificationId()
    }

    override fun cancel() {
        legacyVerifier.cancel()
    }

    override fun setCallbacks(
        onCodeSent: (() -> Unit)?,
        onAutoVerified: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        legacyVerifier.setCallbacks(
            onCodeSent = onCodeSent,
            onAutoVerified = onAutoVerified,
            onError = onError
        )
    }
}

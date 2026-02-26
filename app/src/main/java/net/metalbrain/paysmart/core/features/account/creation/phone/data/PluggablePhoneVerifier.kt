package net.metalbrain.paysmart.core.features.account.creation.phone.data

import android.app.Activity
import android.util.Log

enum class PhoneVerificationPipeline {
    LEGACY_OTP,
    PNV_PREVIEW
}

class PluggablePhoneVerifier(
    private val legacyVerifier: PhoneVerifier,
    private val pnvVerifier: PnvPreviewPhoneVerifier,
    private val pnvPreviewEnabled: Boolean
) : PhoneVerifier {

    private companion object {
        private const val TAG = "PluggablePhoneVerifier"
    }

    private var activeVerifier: PhoneVerifier = legacyVerifier
    private var activePipeline: PhoneVerificationPipeline =
        PhoneVerificationPipeline.LEGACY_OTP

    private var onCodeSent: (() -> Unit)? = null
    private var onAutoVerified: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    override suspend fun start(e164: String, activity: Activity) {
        selectPipeline()
        activeVerifier.setCallbacks(
            onCodeSent = onCodeSent,
            onAutoVerified = onAutoVerified,
            onError = onError
        )
        Log.d(TAG, "Starting verification via pipeline=$activePipeline")
        activeVerifier.start(e164, activity)
    }

    override suspend fun submitOtp(code: String): Result<Unit> {
        return activeVerifier.submitOtp(code)
    }

    override suspend fun resend(e164: String, activity: Activity): Result<Unit> {
        return activeVerifier.resend(e164, activity)
    }

    override fun getVerificationId(): String? {
        return activeVerifier.getVerificationId()
    }

    override fun cancel() {
        legacyVerifier.cancel()
        pnvVerifier.cancel()
        activeVerifier = legacyVerifier
        activePipeline = PhoneVerificationPipeline.LEGACY_OTP
    }

    override fun setCallbacks(
        onCodeSent: (() -> Unit)?,
        onAutoVerified: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        this.onCodeSent = onCodeSent
        this.onAutoVerified = onAutoVerified
        this.onError = onError

        // Keep both configured; active pipeline is selected at start().
        legacyVerifier.setCallbacks(onCodeSent, onAutoVerified, onError)
        pnvVerifier.setCallbacks(onCodeSent, onAutoVerified, onError)
    }

    private fun selectPipeline() {
        val canUsePnv = pnvPreviewEnabled && pnvVerifier.isRuntimeAvailable()
        if (canUsePnv) {
            activeVerifier = pnvVerifier
            activePipeline = PhoneVerificationPipeline.PNV_PREVIEW
            return
        }

        activeVerifier = legacyVerifier
        activePipeline = PhoneVerificationPipeline.LEGACY_OTP
    }
}

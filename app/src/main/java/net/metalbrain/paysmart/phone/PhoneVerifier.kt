package net.metalbrain.paysmart.phone

import android.app.Activity

interface PhoneVerifier {
    suspend fun start(e164: String, activity: Activity)
    suspend fun submitOtp(code: String): Result<Unit>

    suspend fun resend(e164: String, activity: Activity): Result<Unit>

    fun getVerificationId(): String?


    fun cancel()

    fun setCallbacks(
        onCodeSent: (() -> Unit)? = null,
        onAutoVerified: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    )
}

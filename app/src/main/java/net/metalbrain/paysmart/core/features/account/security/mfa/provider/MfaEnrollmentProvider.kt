package net.metalbrain.paysmart.core.features.account.security.mfa.provider

import android.app.Activity
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaEnrollmentChallenge
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaEnrollmentStatus

interface MfaEnrollmentProvider {
    suspend fun loadStatus(): MfaEnrollmentStatus
    suspend fun startSession(): Result<MfaEnrollmentChallenge>
    suspend fun sendVerificationCode(activity: Activity): Result<MfaEnrollmentChallenge>
    suspend fun resendVerificationCode(activity: Activity): Result<Unit>
    suspend fun verifyCodeAndEnroll(code: String, displayName: String? = null): Result<Unit>
    fun clearPendingSession()
}

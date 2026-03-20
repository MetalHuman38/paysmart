package net.metalbrain.paysmart.core.features.account.security.mfa.provider

import android.app.Activity
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import net.metalbrain.paysmart.core.features.account.security.mfa.data.MfaSignInChallenge

interface MfaSignInProvider {
    fun beginChallenge(exception: FirebaseAuthMultiFactorException): Result<MfaSignInChallenge>
    fun getPendingChallenge(): MfaSignInChallenge?
    fun selectFactor(factorUid: String): Result<MfaSignInChallenge>
    suspend fun sendVerificationCode(activity: Activity): Result<MfaSignInChallenge>
    suspend fun resendVerificationCode(activity: Activity): Result<Unit>
    suspend fun verifyCodeAndSignIn(code: String): Result<Unit>
    fun clearPendingChallenge()
}

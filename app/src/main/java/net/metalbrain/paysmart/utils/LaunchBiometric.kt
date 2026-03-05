package net.metalbrain.paysmart.utils

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.R

fun launchBiometricPrompt(context: Context, onSuccess: () -> Unit, onFail: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.biometric_prompt_title))
        .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
        .setNegativeButtonText(context.getString(R.string.biometric_prompt_negative))
        .build()

    val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail()
            }
        })

    biometricPrompt.authenticate(promptInfo)
}

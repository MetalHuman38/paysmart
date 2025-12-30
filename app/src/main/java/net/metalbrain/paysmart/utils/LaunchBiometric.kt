package net.metalbrain.paysmart.utils

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun launchBiometricPrompt(context: Context, onSuccess: () -> Unit, onFail: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock with biometrics")
        .setSubtitle("Use fingerprint or face")
        .setNegativeButtonText("Cancel")
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

package net.metalbrain.paysmart.core.auth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import net.metalbrain.paysmart.utils.FailureCounter
import java.util.concurrent.Executor

object BiometricHelper {

    private val resultChannel = Channel<BiometricResult>(Channel.BUFFERED)
    val biometricResultFlow = resultChannel.receiveAsFlow()

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }

    fun showPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailureLimitReached: (() -> Unit)? = null,
        failureCounter: FailureCounter = FailureCounter()
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                    resultChannel.trySend(BiometricResult.AuthenticationSucceeded)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (failureCounter.increment()) {
                        onFailureLimitReached?.invoke()
                        resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                        return
                    }
                    resultChannel.trySend(
                        BiometricResult.AuthenticationError(
                            errorCode,
                            errString.toString()
                        )
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            })

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false)
            .build()

        prompt.authenticate(info)
    }

    sealed interface BiometricResult {
        data class AuthenticationError(val errorCode: Int, val errorMessage: String) : BiometricResult
        data object AuthenticationSucceeded : BiometricResult
        data object AuthenticationFailed : BiometricResult
        data object AuthenticationNotSet : BiometricResult
    }
}

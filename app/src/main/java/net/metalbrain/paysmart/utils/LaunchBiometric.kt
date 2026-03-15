package net.metalbrain.paysmart.utils

import android.content.Context
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.authorization.biometric.provider.BiometricHelper

fun launchBiometricPrompt(
    context: Context,
    onSuccess: () -> Unit,
    onFail: () -> Unit,
    onError: () -> Unit = onFail
) {
    val activity = context as? FragmentActivity ?: run {
        onError()
        return
    }

    if (!BiometricHelper.isBiometricAvailable(activity)) {
        onError()
        return
    }

    BiometricHelper.showPrompt(
        activity = activity,
        title = context.getString(R.string.biometric_prompt_title),
        subtitle = context.getString(R.string.biometric_prompt_subtitle),
        onSuccess = onSuccess,
        onError = { onError() },
        onFailureLimitReached = onError,
        onAuthenticationFailed = onFail
    )
}

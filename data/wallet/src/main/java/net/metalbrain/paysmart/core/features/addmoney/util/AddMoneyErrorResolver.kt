package net.metalbrain.paysmart.core.features.addmoney.util

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyErrorCode
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiError
import net.metalbrain.paysmart.core.features.addmoney.repository.AddMoneyApiException

enum class AddMoneyFailurePhase {
    CREATE_SESSION,
    REFRESH_STATUS
}

fun resolveAddMoneyUiError(
    throwable: Throwable,
    isLocal: Boolean,
    phase: AddMoneyFailurePhase
): AddMoneyUiError {
    val apiError = throwable as? AddMoneyApiException
    val code = apiError?.code

    return when (code) {
        AddMoneyErrorCode.MISSING_FLUTTERWAVE_SECRET_KEY -> {
            if (isLocal) {
                AddMoneyUiError(
                    title = "Flutterwave emulator setup required",
                    message = "The local payments backend is missing Flutterwave provider credentials.",
                    code = code,
                    supportingText = "Set FLUTTERWAVE_SECRET_KEY or FLUTTERWAVE_CLIENT_ID + FLUTTERWAVE_CLIENT_SECRET in the Functions env, then restart the backend.",
                    isConfigurationIssue = true
                )
            } else {
                AddMoneyUiError(
                    title = "Flutterwave temporarily unavailable",
                    message = "Flutterwave backend credentials are missing.",
                    code = code,
                    isConfigurationIssue = true
                )
            }
        }

        AddMoneyErrorCode.MISSING_FLUTTERWAVE_PUBLIC_KEY -> {
            if (isLocal) {
                AddMoneyUiError(
                    title = "Flutterwave emulator setup required",
                    message = "The local payments backend is missing the Flutterwave public key.",
                    code = code,
                    supportingText = "Set FLUTTERWAVE_PUBLIC_KEY in the Functions env, then restart the backend.",
                    isConfigurationIssue = true
                )
            } else {
                AddMoneyUiError(
                    title = "Flutterwave temporarily unavailable",
                    message = "Flutterwave public key configuration is missing.",
                    code = code,
                    isConfigurationIssue = true
                )
            }
        }

        AddMoneyErrorCode.FLUTTERWAVE_NOT_IMPLEMENTED -> AddMoneyUiError(
            title = "Flutterwave not enabled",
            message = "Flutterwave add money is not fully enabled on the backend yet.",
            code = code,
            isConfigurationIssue = true
        )

        AddMoneyErrorCode.MISSING_STRIPE_SECRET_KEY -> AddMoneyUiError(
            title = if (isLocal) "Stripe emulator setup required" else "Stripe temporarily unavailable",
            message = if (isLocal) {
                "The local payments backend is missing the Stripe secret key."
            } else {
                "Stripe backend credentials are missing."
            },
            code = code,
            supportingText = if (isLocal) {
                "Set STRIPE_SECRET_KEY in the Functions env, then restart the backend."
            } else {
                null
            },
            isConfigurationIssue = true
        )

        AddMoneyErrorCode.MISSING_STRIPE_PUBLISHABLE_KEY -> AddMoneyUiError(
            title = if (isLocal) "Stripe emulator setup required" else "Stripe temporarily unavailable",
            message = if (isLocal) {
                "The local payments backend is missing the Stripe publishable key."
            } else {
                "Stripe publishable key configuration is missing."
            },
            code = code,
            supportingText = if (isLocal) {
                "Set STRIPE_PUBLISHABLE_KEY in the Functions env, then restart the backend."
            } else {
                null
            },
            isConfigurationIssue = true
        )

        AddMoneyErrorCode.INVALID_STRIPE_SECRET_KEY -> AddMoneyUiError(
            title = "Stripe backend configuration is invalid",
            message = "The configured Stripe secret key is not valid for this flow.",
            code = code,
            isConfigurationIssue = true
        )

        AddMoneyErrorCode.SESSION_VALIDATION_UNAVAILABLE -> AddMoneyUiError(
            message = "Session validation is temporarily unavailable. Try again."
        )

        null -> {
            val fallback = when (phase) {
                AddMoneyFailurePhase.CREATE_SESSION -> "Unable to start add money flow"
                AddMoneyFailurePhase.REFRESH_STATUS -> "Unable to refresh payment status"
            }
            val message = throwable.localizedMessage?.takeIf { it.isNotBlank() } ?: fallback
            if (apiError?.statusCode == 503 && isLocal) {
                AddMoneyUiError(
                    title = "Local backend unavailable",
                    message = message,
                    supportingText = "Check the Functions emulator configuration and restart the backend if needed."
                )
            } else {
                AddMoneyUiError(message = message)
            }
        }
    }
}

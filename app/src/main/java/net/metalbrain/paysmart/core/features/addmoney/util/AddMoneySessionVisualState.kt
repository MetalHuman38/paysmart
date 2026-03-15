package net.metalbrain.paysmart.core.features.addmoney.util

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus

internal enum class AddMoneySessionVisualState {
    FLUTTERWAVE_READY,
    SESSION_READY,
    PENDING,
    SUCCEEDED,
    FAILED,
    EXPIRED
}

internal fun resolveAddMoneySessionVisualState(
    status: AddMoneySessionStatus?,
    provider: AddMoneyProvider?
): AddMoneySessionVisualState? {
    return when (status) {
        null -> null
        AddMoneySessionStatus.CREATED -> {
            if (provider == AddMoneyProvider.FLUTTERWAVE) {
                AddMoneySessionVisualState.FLUTTERWAVE_READY
            } else {
                AddMoneySessionVisualState.SESSION_READY
            }
        }

        AddMoneySessionStatus.PENDING -> AddMoneySessionVisualState.PENDING
        AddMoneySessionStatus.SUCCEEDED -> AddMoneySessionVisualState.SUCCEEDED
        AddMoneySessionStatus.FAILED -> AddMoneySessionVisualState.FAILED
        AddMoneySessionStatus.EXPIRED -> AddMoneySessionVisualState.EXPIRED
    }
}

internal fun shouldShowStandaloneSessionInfo(
    status: AddMoneySessionStatus?,
    provider: AddMoneyProvider?,
    infoMessage: String?
): Boolean {
    val message = infoMessage?.trim().orEmpty()
    if (message.isEmpty()) return false
    if (message.contains("canceled", ignoreCase = true)) {
        return true
    }
    return resolveAddMoneySessionVisualState(status, provider) == null
}

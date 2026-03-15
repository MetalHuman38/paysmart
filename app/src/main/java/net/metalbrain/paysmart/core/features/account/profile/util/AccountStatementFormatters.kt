package net.metalbrain.paysmart.core.features.account.profile.util

import kotlin.math.abs

internal fun accountStatementAmountSign(amount: Double): String {
    return if (amount >= 0) "+" else "-"
}

internal fun accountStatementAbsoluteAmount(amount: Double): Double {
    return abs(amount)
}

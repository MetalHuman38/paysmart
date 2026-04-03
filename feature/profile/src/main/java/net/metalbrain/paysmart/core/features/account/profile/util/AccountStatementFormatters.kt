package net.metalbrain.paysmart.core.features.account.profile.util

import kotlin.math.abs

fun accountStatementAmountSign(amount: Double): String {
    return if (amount >= 0) "+" else "-"
}

fun accountStatementAbsoluteAmount(amount: Double): Double {
    return abs(amount)
}

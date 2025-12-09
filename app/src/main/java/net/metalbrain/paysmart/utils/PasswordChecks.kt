package net.metalbrain.paysmart.utils

data class PasswordChecks(
    val lengthOK: Boolean,
    val upperOK: Boolean,
    val lowerOK: Boolean,
    val digitOK: Boolean,
    val symbolOK: Boolean
) {
    val score: Int = listOf(lengthOK, upperOK, lowerOK, digitOK, symbolOK).count { it }

    val allPassed: Boolean get() = score == 5
    val strength: Float get() = score / 5f

    fun label(): String = when (score) {
        0, 1 -> "Very weak"
        2 -> "Weak"
        3 -> "Fair"
        4 -> "Strong"
        5 -> "Very strong"
        else -> "Unknown"
    }
}

fun evaluatePassword(value: String): PasswordChecks {
    return PasswordChecks(
        lengthOK = value.length >= 12,
        upperOK = value.any { it.isUpperCase() },
        lowerOK = value.any { it.isLowerCase() },
        digitOK = value.any { it.isDigit() },
        symbolOK = value.any { !it.isLetterOrDigit() }
    )
}

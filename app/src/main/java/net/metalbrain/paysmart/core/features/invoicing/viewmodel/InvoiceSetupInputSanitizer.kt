package net.metalbrain.paysmart.core.features.invoicing.viewmodel

/**
 * Compares two strings to determine if they represent the same day by comparing
 * their first three characters in a case-insensitive manner after trimming whitespace.
 *
 * @param left The first day string to compare.
 * @param right The second day string to compare.
 * @return `true` if the first three characters of both strings match, `false` otherwise.
 */
internal fun sameDay(left: String, right: String): Boolean {
    return left.trim().lowercase().take(3) == right.trim().lowercase().take(3)
}

/**
 * Sanitizes a string input to ensure it represents a valid decimal format.
 *
 * This function filters the [raw] input to keep only digits and the first occurrence of a
 * decimal point, removing any subsequent dots. The resulting string is truncated to a
 * maximum length of 8 characters.
 *
 * @param raw The raw input string to be sanitized.
 * @return A sanitized string containing only digits and at most one decimal point,
 * limited to 8 characters.
 */
internal fun sanitizeDecimal(raw: String): String {
    val filtered = raw.filter { it.isDigit() || it == '.' }.take(8)
    val dotIndex = filtered.indexOf('.')
    if (dotIndex < 0) return filtered
    val beforeDot = filtered.substring(0, dotIndex + 1)
    val afterDot = filtered.substring(dotIndex + 1).replace(".", "")
    return beforeDot + afterDot
}

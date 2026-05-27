package net.metalbrain.paysmart.core.features.invoicing.viewmodel

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

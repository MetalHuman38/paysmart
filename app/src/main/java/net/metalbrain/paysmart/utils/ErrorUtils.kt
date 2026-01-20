package net.metalbrain.paysmart.utils

fun extractSimpleBackendError(e: Throwable): String {
    val msg = e.message ?: return "Something went wrong"
    return msg.substringAfter("BLOCKING_FUNCTION_ERROR_RESPONSE:", missingDelimiterValue = msg)
        .replace("}", "")
        .replace("{", "")
        .trim()
}

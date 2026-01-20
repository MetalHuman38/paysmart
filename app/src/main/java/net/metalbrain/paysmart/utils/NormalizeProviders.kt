package net.metalbrain.paysmart.utils

fun normalizeProvider(providerId: String): String = when (providerId) {
    "google.com" -> "google"
    "facebook.com" -> "facebook"
    "password" -> "password"
    "phone" -> "phone"
    else -> "unknown"
}

package net.metalbrain.paysmart.utils

fun normalizeProvider(providerId: String): String = when (providerId) {
    "google.com" -> "google"
    "facebook.com" -> "facebook"
    "apple.com" -> "apple"
    "password" -> "password"
    "phone" -> "phone"
    else -> "unknown"
}

package net.metalbrain.paysmart

object Env {

    val apiBase: String
        get() = BuildConfig.API_BASE_URL

    val pvn: Boolean
        get() = BuildConfig.PHONE_PNV_PREVIEW_ENABLED

    val fallbackPublishableKey: String
        get() = BuildConfig.STRIPE_PUBLISHABLE_KEY.trim()

    val identityImageDetectionMode: String
        get() = BuildConfig.IDENTITY_IMAGE_DETECTION_MODE

    val identityImageDetectionFailOpen: Boolean
        get() = BuildConfig.IDENTITY_IMAGE_DETECTION_FAIL_OPEN

    val identityDocumentOcrMode: String
        get() = BuildConfig.IDENTITY_DOCUMENT_OCR_MODE

    val identityDocumentOcrFailOpen: Boolean
        get() = BuildConfig.IDENTITY_DOCUMENT_OCR_FAIL_OPEN

    val funcBase: String
        get() = BuildConfig.FUNCTION_API_URL

    val authBase: String
        get() = apiBase

    val isLocal: Boolean
        get() = BuildConfig.IS_LOCAL
}

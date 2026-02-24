package net.metalbrain.paysmart

import android.util.Log

object Env {

    private const val TAG = "Env"

    val apiBase: String
        get() {
            Log.d(TAG, "API_BASE_URL = ${BuildConfig.API_BASE_URL}")
            return BuildConfig.API_BASE_URL
        }

    val pvn: Boolean
        get() {
            Log.d(TAG, "PVN = ${BuildConfig.PHONE_PNV_PREVIEW_ENABLED}")
            return BuildConfig.PHONE_PNV_PREVIEW_ENABLED
        }

    val identityImageDetectionMode: String
        get() {
            Log.d(TAG, "IDENTITY_IMAGE_DETECTION_MODE = ${BuildConfig.IDENTITY_IMAGE_DETECTION_MODE}")
            return BuildConfig.IDENTITY_IMAGE_DETECTION_MODE
        }

    val identityImageDetectionFailOpen: Boolean
        get() {
            Log.d(TAG, "IDENTITY_IMAGE_DETECTION_FAIL_OPEN = ${BuildConfig.IDENTITY_IMAGE_DETECTION_FAIL_OPEN}")
            return BuildConfig.IDENTITY_IMAGE_DETECTION_FAIL_OPEN
        }

    val funcBase: String
        get() {
            Log.d(TAG, "FUNCTION_API_URL = ${BuildConfig.FUNCTION_API_URL}")
            return BuildConfig.FUNCTION_API_URL
        }

    val authBase: String
        get() = apiBase

    val isLocal: Boolean
        get() = BuildConfig.IS_LOCAL
}

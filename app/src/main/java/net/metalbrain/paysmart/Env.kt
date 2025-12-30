package net.metalbrain.paysmart

import android.util.Log

object Env {

    private const val TAG = "Env"

    val apiBase: String
        get() {
            Log.d(TAG, "API_BASE_URL = ${BuildConfig.API_BASE_URL}")
            return BuildConfig.API_BASE_URL
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

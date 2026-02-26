package net.metalbrain.paysmart.core.auth.appcheck.provider

import okhttp3.Request

suspend fun Request.Builder.attachOptionalAppCheckToken(
    appCheckTokenProvider: AppCheckTokenProvider?
): Request.Builder {
    if (appCheckTokenProvider == null) return this

    val token = appCheckTokenProvider.getTokenOrNull()
    if (!token.isNullOrBlank()) {
        header("X-Firebase-AppCheck", token)
    }
    return this
}

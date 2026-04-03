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

suspend fun Request.Builder.attachRequiredAppCheckToken(
    appCheckTokenProvider: AppCheckTokenProvider?,
    endpointName: String
): Request.Builder {
    requireNotNull(appCheckTokenProvider) {
        "App Check token provider is required for $endpointName"
    }

    val token = appCheckTokenProvider.getTokenOrNull()
    if (token.isNullOrBlank()) {
        throw IllegalStateException("App Check token missing for $endpointName")
    }

    header("X-Firebase-AppCheck", token)
    return this
}

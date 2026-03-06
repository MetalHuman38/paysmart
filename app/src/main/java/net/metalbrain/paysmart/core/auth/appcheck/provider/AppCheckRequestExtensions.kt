package net.metalbrain.paysmart.core.auth.appcheck.provider

import okhttp3.Request

/**
 * Attaches a Firebase App Check token to the request header if a provider is available
 * and a valid token can be retrieved.
 *
 * If the [appCheckTokenProvider] is null or the retrieved token is null or blank,
 * the request remains unmodified.
 *
 * @param appCheckTokenProvider The provider used to retrieve the App Check token, or null.
 * @return This [Request.Builder] instance for chaining.
 */
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

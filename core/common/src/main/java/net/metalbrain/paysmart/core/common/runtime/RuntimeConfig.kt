package net.metalbrain.paysmart.core.common.runtime

interface RuntimeConfig {
    val apiBaseUrl: String
    val functionApiBaseUrl: String
    val authBaseUrl: String
        get() = apiBaseUrl
    val phonePnvPreviewEnabled: Boolean
    val stripePublishableKey: String
    val identityImageDetectionMode: String
    val identityImageDetectionFailOpen: Boolean
    val identityDocumentOcrMode: String
    val identityDocumentOcrFailOpen: Boolean
    val isLocal: Boolean
    val appCheckEnforced: Boolean
    val isDebug: Boolean
}

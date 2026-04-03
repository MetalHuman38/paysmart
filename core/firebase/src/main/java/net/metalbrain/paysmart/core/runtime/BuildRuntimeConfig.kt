package net.metalbrain.paysmart.core.runtime

import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.firebase.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildRuntimeConfig @Inject constructor() : RuntimeConfig {
    override val apiBaseUrl: String
        get() = BuildConfig.API_BASE_URL

    override val functionApiBaseUrl: String
        get() = BuildConfig.FUNCTION_API_URL

    override val phonePnvPreviewEnabled: Boolean
        get() = BuildConfig.PHONE_PNV_PREVIEW_ENABLED

    override val stripePublishableKey: String
        get() = BuildConfig.STRIPE_PUBLISHABLE_KEY.trim()

    override val identityImageDetectionMode: String
        get() = BuildConfig.IDENTITY_IMAGE_DETECTION_MODE

    override val identityImageDetectionFailOpen: Boolean
        get() = BuildConfig.IDENTITY_IMAGE_DETECTION_FAIL_OPEN

    override val identityDocumentOcrMode: String
        get() = BuildConfig.IDENTITY_DOCUMENT_OCR_MODE

    override val identityDocumentOcrFailOpen: Boolean
        get() = BuildConfig.IDENTITY_DOCUMENT_OCR_FAIL_OPEN

    override val isLocal: Boolean
        get() = BuildConfig.IS_LOCAL

    override val appCheckEnforced: Boolean
        get() = BuildConfig.APP_CHECK_ENFORCED

    override val isDebug: Boolean
        get() = BuildConfig.IS_DEBUG_BUILD
}

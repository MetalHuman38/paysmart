package net.metalbrain.paysmart.core.service.update

import javax.inject.Inject
import javax.inject.Singleton
import net.metalbrain.paysmart.core.service.repositiry.FirebaseAnalyticalServiceInterface

interface UpdateAnalyticsLogger {
    fun logEvent(event: String, params: Map<String, Any?> = emptyMap())
    fun logNonFatal(tag: String, throwable: Throwable, extras: Map<String, String> = emptyMap())
}

@Singleton
class FirebaseUpdateAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalyticalServiceInterface,
) : UpdateAnalyticsLogger {
    override fun logEvent(event: String, params: Map<String, Any?>) {
        analytics.logEvent(
            event = "app_update_$event",
            params = params,
        )
    }

    override fun logNonFatal(
        tag: String,
        throwable: Throwable,
        extras: Map<String, String>,
    ) {
        analytics.logNonFatal(
            tag = "app_update_$tag",
            throwable = throwable,
            extras = extras,
        )
    }
}

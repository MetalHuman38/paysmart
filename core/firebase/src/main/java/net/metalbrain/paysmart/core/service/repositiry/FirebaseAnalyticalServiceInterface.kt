package net.metalbrain.paysmart.core.service.repositiry

interface FirebaseAnalyticalServiceInterface {
    fun setUserID(uid: String)
    fun setUserProperty(name: String, value: String)
    fun logEvent(event: String, params: Map<String, Any?> = emptyMap())
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun logNonFatal(
        tag: String,
        throwable: Throwable,
        extras: Map<String, String> = emptyMap()
    )
}

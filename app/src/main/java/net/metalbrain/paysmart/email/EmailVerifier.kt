package net.metalbrain.paysmart.email


interface EmailVerifier {
    suspend fun start(email: String)

    suspend fun checkStatus(): Boolean

    fun setCallbacks(
        onLinkSent: (() -> Unit)? = null,
        onVerified: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    )
}

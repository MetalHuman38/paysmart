package net.metalbrain.paysmart.core.notifications

object NotificationChannels {
    const val ACCOUNT_UPDATES = "paysmart.account_updates"
    const val PRODUCT_UPDATES = "paysmart.product_updates"
    const val APP_UPDATES = "paysmart.app_updates"

    fun resolveChannelId(rawChannel: String?): String {
        return when (rawChannel?.trim()?.lowercase()) {
            "product_updates" -> PRODUCT_UPDATES
            "app_updates" -> APP_UPDATES
            else -> ACCOUNT_UPDATES
        }
    }
}

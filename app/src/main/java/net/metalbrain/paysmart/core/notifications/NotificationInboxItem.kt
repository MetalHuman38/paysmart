package net.metalbrain.paysmart.core.notifications

data class NotificationInboxItem(
    val notificationId: String,
    val source: String,
    val type: String,
    val channel: String,
    val title: String,
    val body: String,
    val deepLink: String?,
    val isUnread: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

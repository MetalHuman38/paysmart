package net.metalbrain.paysmart.core.features.notifications.state

import net.metalbrain.paysmart.core.notifications.NotificationInboxItem

data class NotificationCenterUiState(
    val notifications: List<NotificationInboxItem> = emptyList(),
    val unreadCount: Int = 0,
)

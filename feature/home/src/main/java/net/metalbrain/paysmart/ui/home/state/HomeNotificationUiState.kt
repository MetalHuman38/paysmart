package net.metalbrain.paysmart.ui.home.state

enum class HomeNotificationKind {
    PLACEHOLDER,
    INBOX,
    APP_UPDATE_READY
}

data class HomeNotificationUiState(
    val kind: HomeNotificationKind = HomeNotificationKind.PLACEHOLDER,
    val title: String = "",
    val body: String = "",
    val unreadCount: Int = 0,
    val isUnread: Boolean = false
)

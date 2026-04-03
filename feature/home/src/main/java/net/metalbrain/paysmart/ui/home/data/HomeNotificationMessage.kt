package net.metalbrain.paysmart.ui.home.data

data class HomeNotificationMessage(
    val type: String,
    val title: String,
    val body: String,
    val isUnread: Boolean,
)

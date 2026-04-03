package net.metalbrain.paysmart.ui.home.data

import kotlinx.coroutines.flow.Flow

interface HomeNotificationGateway {
    fun observeLatestNotification(): Flow<HomeNotificationMessage?>

    fun observeUnreadCount(): Flow<Int>

    companion object {
        const val TYPE_APP_UPDATE_READY = "app_update_ready"
    }
}

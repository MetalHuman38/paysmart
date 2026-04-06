package net.metalbrain.paysmart.core.notifications

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.ui.home.data.HomeNotificationGateway
import net.metalbrain.paysmart.ui.home.data.HomeNotificationMessage

@Singleton
class HomeNotificationGatewayAdapter @Inject constructor(
    private val notificationInboxRepository: NotificationInboxRepository,
) : HomeNotificationGateway {

    override fun observeLatestNotification(): Flow<HomeNotificationMessage?> {
        return notificationInboxRepository.observeLatestNotification()
            .map { item ->
                item?.let { notification ->
                    HomeNotificationMessage(
                        type = notification.type,
                        title = notification.title,
                        body = notification.body,
                        isUnread = notification.isUnread,
                    )
                }
            }
    }

    override fun observeUnreadCount(): Flow<Int> {
        return notificationInboxRepository.observeUnreadCount()
    }
}

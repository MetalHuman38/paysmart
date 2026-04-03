package net.metalbrain.paysmart.core.notifications

import jakarta.inject.Inject
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailVerificationNotificationGateway

class EmailVerificationNotificationGatewayAdapter @Inject constructor(
    private val notificationInboxRepository: NotificationInboxRepository,
) : EmailVerificationNotificationGateway {

    override suspend fun syncEmailVerifiedNotification(uid: String, email: String?) {
        notificationInboxRepository.syncEmailVerifiedNotification(uid = uid, email = email)
    }
}

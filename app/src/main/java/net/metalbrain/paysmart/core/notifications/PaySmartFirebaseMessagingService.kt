package net.metalbrain.paysmart.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class PaySmartFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationBootstrapper: NotificationBootstrapper

    @Inject
    lateinit var pushMessageNotifier: PushMessageNotifier

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        notificationBootstrapper.onNewFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        pushMessageNotifier.show(message)
    }
}

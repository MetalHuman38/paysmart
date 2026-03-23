package net.metalbrain.paysmart.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.metalbrain.paysmart.MainActivity
import net.metalbrain.paysmart.R

@Singleton
class PushMessageNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun show(remoteMessage: RemoteMessage) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        val title = remoteMessage.notification?.title
            ?.trim()
            .orEmpty()
            .ifBlank { remoteMessage.data["title"].orEmpty().trim() }
        val body = remoteMessage.notification?.body
            ?.trim()
            .orEmpty()
            .ifBlank { remoteMessage.data["body"].orEmpty().trim() }
        if (title.isBlank() || body.isBlank()) {
            return
        }

        val channelId = NotificationChannels.resolveChannelId(remoteMessage.data["channel"])
        val notificationId = remoteMessage.data["notificationId"]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: remoteMessage.messageId
            ?: "${channelId}_${System.currentTimeMillis()}"

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            remoteMessage.data["deepLink"]?.trim()?.takeIf { it.isNotEmpty() }?.let { deepLink ->
                putExtra(EXTRA_NOTIFICATION_DEEP_LINK, deepLink)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == NotificationChannels.APP_UPDATES) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_NOTIFICATION_DEEP_LINK = "notification_deep_link"
    }
}

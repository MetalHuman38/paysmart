package net.metalbrain.paysmart.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import net.metalbrain.paysmart.R

object NotificationChannelRegistrar {

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(
                NotificationChannels.ACCOUNT_UPDATES,
                context.getString(R.string.notification_channel_account_updates_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_account_updates_description)
            },
            NotificationChannel(
                NotificationChannels.PRODUCT_UPDATES,
                context.getString(R.string.notification_channel_product_updates_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_product_updates_description)
            },
            NotificationChannel(
                NotificationChannels.APP_UPDATES,
                context.getString(R.string.notification_channel_app_updates_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_app_updates_description)
            }
        )

        notificationManager.createNotificationChannels(channels)
    }
}

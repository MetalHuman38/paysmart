package net.metalbrain.paysmart.core.features.notifications.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.notifications.NotificationChannels
import net.metalbrain.paysmart.core.notifications.NotificationInboxItem
import net.metalbrain.paysmart.core.notifications.NotificationInboxRepository
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A composable card that displays a single notification item within the Notification Center.
 *
 * This component renders the notification's icon, title, timestamp, body text, and an unread
 * indicator if applicable. It dynamically adjusts its icon and accent color based on the
 * notification's type or channel. For specific notification types, such as app updates,
 * it provides a primary action button.
 *
 * @param item The notification data to display, including its status, content, and metadata.
 * @param onPrimaryAction The callback to be invoked when the primary action button
 * (e.g., "Restart" for app updates) is clicked.
 */
@Composable
fun NotificationCenterCard(
    item: NotificationInboxItem,
    onPrimaryAction: () -> Unit,
) {
    val accentColor = notificationAccent(item)
    NotificationSurfaceCard(
        accentColor = accentColor,
        highlighted = item.isUnread
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.14f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificationIcon(item),
                    contentDescription = null,
                    tint = accentColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatNotificationTimestamp(item.createdAtMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.isUnread) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = stringResource(R.string.notification_center_unread_label),
                        modifier = Modifier.padding(horizontal = Dimens.sm, vertical = Dimens.xs),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text(
            text = item.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (item.type == NotificationInboxRepository.TYPE_APP_UPDATE_READY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onPrimaryAction) {
                    Text(
                        text = stringResource(R.string.app_update_restart_action),
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun notificationIcon(item: NotificationInboxItem): ImageVector {
    return when (item.type) {
        NotificationInboxRepository.TYPE_APP_UPDATE_READY -> Icons.Default.SystemUpdate
        NotificationInboxRepository.TYPE_EMAIL_VERIFIED -> Icons.Default.MarkEmailRead
        else -> when (item.channel) {
            NotificationChannels.PRODUCT_UPDATES -> Icons.Default.TipsAndUpdates
            else -> Icons.Default.Notifications
        }
    }
}

private fun notificationAccent(item: NotificationInboxItem): Color {
    return when (item.type) {
        NotificationInboxRepository.TYPE_APP_UPDATE_READY -> Color(0xFF6FA9FF)
        NotificationInboxRepository.TYPE_EMAIL_VERIFIED -> Color(0xFF41C995)
        else -> when (item.channel) {
            NotificationChannels.PRODUCT_UPDATES -> Color(0xFFF2B84B)
            else -> Color(0xFF7FC4C8)
        }
    }
}

private fun formatNotificationTimestamp(timestampMs: Long): String {
    return Instant.ofEpochMilli(timestampMs)
        .atZone(ZoneId.systemDefault())
        .format(notificationTimestampFormatter)
}

private val notificationTimestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

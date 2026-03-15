package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.state.HomeNotificationKind
import net.metalbrain.paysmart.ui.home.state.HomeNotificationUiState
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun HomeNotificationPanel(
    notification: HomeNotificationUiState,
    onPrimaryAction: () -> Unit,
) {
    val title = when (notification.kind) {
        HomeNotificationKind.PRODUCT -> stringResource(R.string.home_notification_products_title)
        HomeNotificationKind.APP_UPDATE_READY -> stringResource(R.string.home_notification_update_title)
    }
    val body = when (notification.kind) {
        HomeNotificationKind.PRODUCT -> stringResource(R.string.home_notification_products_body)
        HomeNotificationKind.APP_UPDATE_READY -> stringResource(R.string.app_update_downloaded_message)
    }
    val icon = when (notification.kind) {
        HomeNotificationKind.PRODUCT -> Icons.Default.TipsAndUpdates
        HomeNotificationKind.APP_UPDATE_READY -> Icons.Default.Update
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(Dimens.space10)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (notification.kind == HomeNotificationKind.APP_UPDATE_READY) {
                TextButton(onClick = onPrimaryAction) {
                    Text(text = stringResource(R.string.app_update_restart_action))
                }
            }
        }
    }
}

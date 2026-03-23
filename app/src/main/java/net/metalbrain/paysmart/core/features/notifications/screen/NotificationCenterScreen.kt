package net.metalbrain.paysmart.core.features.notifications.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.notifications.cards.NotificationCenterCard
import net.metalbrain.paysmart.core.features.notifications.cards.NotificationCenterSummaryCard
import net.metalbrain.paysmart.core.features.notifications.state.NotificationCenterEmptyState
import net.metalbrain.paysmart.core.features.notifications.viewmodel.NotificationCenterViewModel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun NotificationCenterScreen(
    viewModel: NotificationCenterViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.md),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + Dimens.md,
            end = Dimens.screenPadding,
            bottom = Dimens.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = stringResource(R.string.notification_center_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (uiState.unreadCount > 0) {
                        stringResource(R.string.notification_center_unread_label)
                    } else {
                        stringResource(R.string.notification_center_all_caught_up)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            NotificationCenterSummaryCard(unreadCount = uiState.unreadCount)
        }

        if (uiState.notifications.isEmpty()) {
            item {
                NotificationCenterEmptyState()
            }
        } else {
            items(
                items = uiState.notifications,
                key = { item -> item.notificationId }
            ) { item ->
                NotificationCenterCard(
                    item = item,
                    onPrimaryAction = { viewModel.onNotificationAction(item) }
                )
            }
        }
    }
}

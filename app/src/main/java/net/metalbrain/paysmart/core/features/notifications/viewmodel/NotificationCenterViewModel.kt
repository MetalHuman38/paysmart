package net.metalbrain.paysmart.core.features.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.notifications.state.NotificationCenterUiState
import net.metalbrain.paysmart.core.notifications.NotificationInboxItem
import net.metalbrain.paysmart.core.notifications.NotificationInboxRepository
import net.metalbrain.paysmart.core.service.update.UpdateCoordinator

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val notificationInboxRepository: NotificationInboxRepository,
    private val updateCoordinator: UpdateCoordinator,
) : ViewModel() {

    val uiState: StateFlow<NotificationCenterUiState> = combine(
        notificationInboxRepository.observeNotifications(),
        notificationInboxRepository.observeUnreadCount(),
    ) { notifications, unreadCount ->
        NotificationCenterUiState(
            notifications = notifications,
            unreadCount = unreadCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationCenterUiState()
    )

    init {
        viewModelScope.launch {
            notificationInboxRepository.markAllRead()
        }
    }

    fun onNotificationAction(item: NotificationInboxItem) {
        viewModelScope.launch {
            notificationInboxRepository.markAsRead(item.notificationId)
            if (item.type == NotificationInboxRepository.TYPE_APP_UPDATE_READY) {
                updateCoordinator.completeUpdate()
            }
        }
    }
}

package net.metalbrain.paysmart.core.notifications

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.service.update.UpdateCoordinator
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class NotificationBootstrapper @Inject constructor(
    private val notificationInboxRepository: NotificationInboxRepository,
    private val notificationInstallationRegistrar: NotificationInstallationRegistrar,
    private val notificationInstallationStore: NotificationInstallationStore,
    private val userManager: UserManager,
    private val updateCoordinator: UpdateCoordinator,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }

        notificationInboxRepository.start()
        scope.launch {
            userManager.authState.collect { authState ->
                if (authState is AuthState.Authenticated) {
                    runCatching {
                        notificationInstallationRegistrar.registerCurrentInstallation()
                    }
                }
            }
        }
        scope.launch {
            combine(userManager.authState, updateCoordinator.uiState) { authState, updateUiState ->
                authState to updateUiState
            }.collect { (authState, updateUiState) ->
                if (authState is AuthState.Authenticated) {
                    runCatching {
                        notificationInboxRepository.syncAppUpdateNotification(
                            uid = authState.uid,
                            showRestartPrompt = updateUiState.showRestartPrompt,
                            versionCode = updateUiState.downloadedVersionCode
                        )
                    }
                }
            }
        }
    }

    fun syncRegistrationIfPossible(force: Boolean = false) {
        scope.launch {
            runCatching {
                notificationInstallationRegistrar.registerCurrentInstallation(force = force)
            }
        }
    }

    fun onNewFcmToken(token: String) {
        scope.launch {
            runCatching {
                notificationInstallationRegistrar.cacheAndRegisterToken(token)
            }
        }
    }

    fun shouldRequestNotificationPermission(): Boolean {
        return !notificationInstallationRegistrar.notificationsPermissionGranted() &&
            !notificationInstallationStore.hasShownPermissionPrompt()
    }

    fun markPermissionPromptShown() {
        notificationInstallationStore.markPermissionPromptShown()
    }

    fun onNotificationPermissionResult() {
        syncRegistrationIfPossible(force = true)
    }
}

package net.metalbrain.paysmart.core.service.update

import jakarta.inject.Inject
import net.metalbrain.paysmart.core.features.notifications.data.NotificationUpdateActionGateway

class NotificationUpdateActionGatewayAdapter @Inject constructor(
    private val updateCoordinator: UpdateCoordinator,
) : NotificationUpdateActionGateway {

    override fun completeAppUpdate() {
        updateCoordinator.completeUpdate()
    }
}

package net.metalbrain.paysmart.core.service.update

import javax.inject.Inject
import javax.inject.Singleton

sealed interface UpdateDecision {
    data object None : UpdateDecision

    data class Start(
        val updateType: UpdateType,
        val resumeInProgress: Boolean = false,
    ) : UpdateDecision
}

@Singleton
class UpdatePolicyEngine @Inject constructor(
    private val configProvider: UpdatePolicyConfigProvider,
) {
    fun evaluate(
        snapshot: UpdateInfoSnapshot,
        appState: UpdateAppState,
    ): UpdateDecision {
        if (snapshot.availability == UpdateAvailabilityStatus.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            return UpdateDecision.Start(
                updateType = UpdateType.IMMEDIATE,
                resumeInProgress = true,
            )
        }

        val config = configProvider.getConfig()
        if (!config.enabled || appState != UpdateAppState.SAFE) {
            return UpdateDecision.None
        }

        if (snapshot.availability != UpdateAvailabilityStatus.UPDATE_AVAILABLE) {
            return UpdateDecision.None
        }

        val staleDays = snapshot.clientVersionStalenessDays ?: 0
        val immediateGate = snapshot.isImmediateAllowed && meetsThreshold(
            priority = snapshot.updatePriority,
            staleDays = staleDays,
            minPriority = config.immediateMinPriority,
            minStalenessDays = config.immediateMinStalenessDays,
        )
        if (immediateGate) {
            return UpdateDecision.Start(UpdateType.IMMEDIATE)
        }

        val flexibleGate = meetsThreshold(
            priority = snapshot.updatePriority,
            staleDays = staleDays,
            minPriority = config.flexibleMinPriority,
            minStalenessDays = config.flexibleMinStalenessDays,
        )
        if (flexibleGate && snapshot.isFlexibleAllowed) {
            return UpdateDecision.Start(UpdateType.FLEXIBLE)
        }
        if (flexibleGate && snapshot.isImmediateAllowed) {
            return UpdateDecision.Start(UpdateType.IMMEDIATE)
        }

        return UpdateDecision.None
    }

    fun immediateRetryCooldownMillis(): Long {
        return configProvider.getConfig().immediateRetryCooldownMinutes * 60_000L
    }

    private fun meetsThreshold(
        priority: Int,
        staleDays: Int,
        minPriority: Int,
        minStalenessDays: Int,
    ): Boolean {
        val meetsPriority = minPriority > 0 && priority >= minPriority
        val meetsStaleness = staleDays >= minStalenessDays
        return meetsPriority || meetsStaleness
    }
}

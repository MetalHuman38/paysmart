package net.metalbrain.paysmart.core.service.update

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatePolicyEngineTest {

    private val engine = UpdatePolicyEngine(
        configProvider = StaticUpdatePolicyConfigProvider(
            config = UpdatePolicyConfig(
                flexibleMinStalenessDays = 2,
                flexibleMinPriority = 0,
                immediateMinStalenessDays = 7,
                immediateMinPriority = 4,
            )
        )
    )

    @Test
    fun `resumes immediate update when play reports update in progress`() {
        val decision = engine.evaluate(
            snapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ),
            appState = UpdateAppState.CRITICAL,
        )

        assertEquals(
            UpdateDecision.Start(
                updateType = UpdateType.IMMEDIATE,
                resumeInProgress = true,
            ),
            decision,
        )
    }

    @Test
    fun `blocks new prompts while app state is critical`() {
        val decision = engine.evaluate(
            snapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                clientVersionStalenessDays = 10,
                updatePriority = 5,
                isImmediateAllowed = true,
                isFlexibleAllowed = true,
            ),
            appState = UpdateAppState.CRITICAL,
        )

        assertEquals(UpdateDecision.None, decision)
    }

    @Test
    fun `uses immediate updates when priority threshold is met`() {
        val decision = engine.evaluate(
            snapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                clientVersionStalenessDays = 1,
                updatePriority = 5,
                isImmediateAllowed = true,
                isFlexibleAllowed = true,
            ),
            appState = UpdateAppState.SAFE,
        )

        assertEquals(
            UpdateDecision.Start(UpdateType.IMMEDIATE),
            decision,
        )
    }

    @Test
    fun `uses flexible updates when stale enough and app state is safe`() {
        val decision = engine.evaluate(
            snapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                clientVersionStalenessDays = 3,
                updatePriority = 0,
                isImmediateAllowed = true,
                isFlexibleAllowed = true,
            ),
            appState = UpdateAppState.SAFE,
        )

        assertEquals(
            UpdateDecision.Start(UpdateType.FLEXIBLE),
            decision,
        )
    }
}

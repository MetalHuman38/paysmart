package net.metalbrain.paysmart.core.service.update

import android.app.Activity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCoordinatorTest {

    private val launcher = TestIntentSenderLauncher()

    @Test
    fun `does not start new update flows during critical routes`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                availableVersionCode = 42,
                clientVersionStalenessDays = 10,
                updatePriority = 5,
                isImmediateAllowed = true,
                isFlexibleAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.onAppStateChanged(UpdateAppState.CRITICAL)
        coordinator.onAppForegrounded(launcher)

        assertTrue(dataSource.startRequests.isEmpty())
    }

    @Test
    fun `starts flexible update only once per version`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                availableVersionCode = 11,
                clientVersionStalenessDays = 3,
                updatePriority = 0,
                isImmediateAllowed = true,
                isFlexibleAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.onAppStateChanged(UpdateAppState.SAFE)
        coordinator.onAppForegrounded(launcher)
        coordinator.onAppForegrounded(launcher)

        assertEquals(listOf(UpdateType.FLEXIBLE), dataSource.startRequests)
    }

    @Test
    fun `resumes immediate update even while route is critical`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
                availableVersionCode = 73,
                isImmediateAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.onAppStateChanged(UpdateAppState.CRITICAL)
        coordinator.onAppForegrounded(launcher)

        assertEquals(listOf(UpdateType.IMMEDIATE), dataSource.startRequests)
    }

    @Test
    fun `immediate decline enters cooldown and suppresses duplicate prompt`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                availableVersionCode = 77,
                clientVersionStalenessDays = 10,
                updatePriority = 5,
                isImmediateAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.onAppStateChanged(UpdateAppState.SAFE)
        coordinator.onAppForegrounded(launcher)
        coordinator.onActivityResult(Activity.RESULT_CANCELED)
        coordinator.onAppForegrounded(launcher)

        assertEquals(listOf(UpdateType.IMMEDIATE), dataSource.startRequests)
    }

    @Test
    fun `downloaded flexible update shows restart prompt only on safe routes`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                availableVersionCode = 99,
                clientVersionStalenessDays = 3,
                isFlexibleAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.startObserving()
        coordinator.onAppStateChanged(UpdateAppState.CRITICAL)
        coordinator.onAppForegrounded(launcher)
        dataSource.dispatchInstallState(
            UpdateInstallState(installStatus = UpdateInstallStatus.DOWNLOADED)
        )

        assertFalse(coordinator.uiState.value.showRestartPrompt)

        coordinator.onAppStateChanged(UpdateAppState.SAFE)

        assertTrue(coordinator.uiState.value.showRestartPrompt)
        assertEquals(99, coordinator.uiState.value.downloadedVersionCode)
    }

    @Test
    fun `acknowledging restart prompt avoids duplicates and completeUpdate delegates`() = runTest {
        val dataSource = FakeUpdateDataSource(
            currentSnapshot = UpdateInfoSnapshot(
                availability = UpdateAvailabilityStatus.UPDATE_AVAILABLE,
                availableVersionCode = 101,
                clientVersionStalenessDays = 3,
                isFlexibleAllowed = true,
            )
        )
        val coordinator = createCoordinator(dataSource)

        coordinator.startObserving()
        coordinator.onAppStateChanged(UpdateAppState.SAFE)
        coordinator.onAppForegrounded(launcher)
        dataSource.dispatchInstallState(
            UpdateInstallState(installStatus = UpdateInstallStatus.DOWNLOADED)
        )
        coordinator.acknowledgeRestartPrompt()
        coordinator.completeUpdate()
        coordinator.onAppStateChanged(UpdateAppState.CRITICAL)
        coordinator.onAppStateChanged(UpdateAppState.SAFE)

        assertFalse(coordinator.uiState.value.showRestartPrompt)
        assertTrue(dataSource.completeCalled)
    }

    @Test
    fun `registers and unregisters install listener correctly`() {
        val dataSource = FakeUpdateDataSource()
        val coordinator = createCoordinator(dataSource)

        coordinator.startObserving()
        assertTrue(dataSource.listenerRegistered)

        coordinator.stopObserving()
        assertFalse(dataSource.listenerRegistered)
    }

    private fun createCoordinator(
        dataSource: FakeUpdateDataSource,
    ): UpdateCoordinator {
        return UpdateCoordinator(
            updateDataSource = dataSource,
            updatePolicyEngine = UpdatePolicyEngine(
                configProvider = StaticUpdatePolicyConfigProvider(
                    config = UpdatePolicyConfig(
                        flexibleMinStalenessDays = 2,
                        immediateMinStalenessDays = 7,
                        immediateMinPriority = 4,
                    )
                )
            ),
            analyticsLogger = FakeUpdateAnalyticsLogger(),
            updateClock = FakeUpdateClock(),
        )
    }
}

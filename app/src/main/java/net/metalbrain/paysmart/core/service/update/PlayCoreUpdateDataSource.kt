package net.metalbrain.paysmart.core.service.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class PlayCoreUpdateDataSource @Inject constructor(
    @ApplicationContext context: Context,
) : UpdateDataSource {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private var installStateListener: InstallStateUpdatedListener? = null

    override suspend fun getUpdateInfo(): UpdateInfoSnapshot {
        return appUpdateManager.appUpdateInfo.await().toSnapshot()
    }

    override suspend fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: UpdateType,
    ): Boolean {
        val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
        val updateOptions = AppUpdateOptions.newBuilder(updateType.toPlayCoreType())
            .setAllowAssetPackDeletion(false)
            .build()
        return appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            launcher,
            updateOptions,
        )
    }

    override fun registerListener(listener: (UpdateInstallState) -> Unit) {
        unregisterListener()
        val wrappedListener = InstallStateUpdatedListener { state ->
            listener(
                UpdateInstallState(
                    installStatus = state.installStatus().toUpdateInstallStatus(),
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload(),
                )
            )
        }
        installStateListener = wrappedListener
        appUpdateManager.registerListener(wrappedListener)
    }

    override fun unregisterListener() {
        val registeredListener = installStateListener ?: return
        appUpdateManager.unregisterListener(registeredListener)
        installStateListener = null
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}

private fun AppUpdateInfo.toSnapshot(): UpdateInfoSnapshot {
    return UpdateInfoSnapshot(
        availability = updateAvailability().toUpdateAvailabilityStatus(),
        installStatus = installStatus().toUpdateInstallStatus(),
        availableVersionCode = availableVersionCode(),
        clientVersionStalenessDays = clientVersionStalenessDays(),
        updatePriority = updatePriority(),
        isImmediateAllowed = isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
        isFlexibleAllowed = isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
    )
}

private fun UpdateType.toPlayCoreType(): Int {
    return when (this) {
        UpdateType.FLEXIBLE -> AppUpdateType.FLEXIBLE
        UpdateType.IMMEDIATE -> AppUpdateType.IMMEDIATE
    }
}

private fun Int.toUpdateAvailabilityStatus(): UpdateAvailabilityStatus {
    return when (this) {
        UpdateAvailability.UPDATE_NOT_AVAILABLE -> UpdateAvailabilityStatus.UPDATE_NOT_AVAILABLE
        UpdateAvailability.UPDATE_AVAILABLE -> UpdateAvailabilityStatus.UPDATE_AVAILABLE
        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
            UpdateAvailabilityStatus.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

        else -> UpdateAvailabilityStatus.UNKNOWN
    }
}

private fun Int.toUpdateInstallStatus(): UpdateInstallStatus {
    return when (this) {
        InstallStatus.PENDING -> UpdateInstallStatus.PENDING
        InstallStatus.DOWNLOADING -> UpdateInstallStatus.DOWNLOADING
        InstallStatus.DOWNLOADED -> UpdateInstallStatus.DOWNLOADED
        InstallStatus.INSTALLING -> UpdateInstallStatus.INSTALLING
        InstallStatus.INSTALLED -> UpdateInstallStatus.INSTALLED
        InstallStatus.FAILED -> UpdateInstallStatus.FAILED
        InstallStatus.CANCELED -> UpdateInstallStatus.CANCELED
        else -> UpdateInstallStatus.UNKNOWN
    }
}

package net.metalbrain.paysmart.core.service.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

interface UpdateDataSource {
    suspend fun getUpdateInfo(): UpdateInfoSnapshot

    suspend fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: UpdateType,
    ): Boolean

    fun registerListener(listener: (UpdateInstallState) -> Unit)

    fun unregisterListener()

    fun completeUpdate()
}

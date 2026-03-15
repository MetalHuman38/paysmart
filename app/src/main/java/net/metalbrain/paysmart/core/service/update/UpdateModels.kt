package net.metalbrain.paysmart.core.service.update

enum class UpdateAppState {
    UNKNOWN,
    SAFE,
    CRITICAL,
}

enum class UpdateType {
    FLEXIBLE,
    IMMEDIATE,
}

enum class UpdateAvailabilityStatus {
    UNKNOWN,
    UPDATE_NOT_AVAILABLE,
    UPDATE_AVAILABLE,
    DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
}

enum class UpdateInstallStatus {
    UNKNOWN,
    PENDING,
    DOWNLOADING,
    DOWNLOADED,
    INSTALLING,
    INSTALLED,
    FAILED,
    CANCELED,
}

data class UpdateInfoSnapshot(
    val availability: UpdateAvailabilityStatus = UpdateAvailabilityStatus.UNKNOWN,
    val installStatus: UpdateInstallStatus = UpdateInstallStatus.UNKNOWN,
    val availableVersionCode: Int? = null,
    val clientVersionStalenessDays: Int? = null,
    val updatePriority: Int = 0,
    val isImmediateAllowed: Boolean = false,
    val isFlexibleAllowed: Boolean = false,
)

data class UpdateInstallState(
    val installStatus: UpdateInstallStatus = UpdateInstallStatus.UNKNOWN,
    val bytesDownloaded: Long = 0L,
    val totalBytesToDownload: Long = 0L,
)

data class UpdateUiState(
    val appState: UpdateAppState = UpdateAppState.UNKNOWN,
    val showRestartPrompt: Boolean = false,
    val downloadedVersionCode: Int? = null,
    val installStatus: UpdateInstallStatus = UpdateInstallStatus.UNKNOWN,
)

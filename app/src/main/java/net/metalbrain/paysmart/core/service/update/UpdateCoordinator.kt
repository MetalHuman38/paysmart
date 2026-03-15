package net.metalbrain.paysmart.core.service.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UpdateCoordinator @Inject constructor(
    private val updateDataSource: UpdateDataSource,
    private val updatePolicyEngine: UpdatePolicyEngine,
    private val analyticsLogger: UpdateAnalyticsLogger,
    private val updateClock: UpdateClock,
) {
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var currentAppState: UpdateAppState = UpdateAppState.UNKNOWN
    private var currentInstallStatus: UpdateInstallStatus = UpdateInstallStatus.UNKNOWN
    private var downloadedVersionCode: Int? = null
    private var lastKnownVersionCode: Int? = null
    private var lastLaunchedRequest: UpdatePromptRecord? = null
    private var lastImmediateDeclinedVersionCode: Int? = null
    private var lastImmediateDeclinedAtMs: Long = 0L
    private val promptedFlexibleVersions = mutableSetOf<Int>()
    private val acknowledgedRestartPromptVersions = mutableSetOf<Int>()
    private var observingInstallState = false

    fun onAppStateChanged(appState: UpdateAppState) {
        currentAppState = appState
        syncUiState()
    }

    fun startObserving() {
        if (observingInstallState) {
            return
        }
        updateDataSource.registerListener(::onInstallStateChanged)
        observingInstallState = true
    }

    fun stopObserving() {
        if (!observingInstallState) {
            return
        }
        updateDataSource.unregisterListener()
        observingInstallState = false
    }

    suspend fun onAppForegrounded(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val snapshot = runCatching { updateDataSource.getUpdateInfo() }
            .onFailure { error ->
                analyticsLogger.logNonFatal(
                    tag = "query_failed",
                    throwable = error,
                    extras = mapOf("app_state" to currentAppState.name.lowercase()),
                )
            }
            .getOrNull() ?: return

        lastKnownVersionCode = snapshot.availableVersionCode ?: lastKnownVersionCode
        currentInstallStatus = snapshot.installStatus

        when (snapshot.installStatus) {
            UpdateInstallStatus.DOWNLOADED -> {
                downloadedVersionCode = snapshot.availableVersionCode ?: downloadedVersionCode ?: lastKnownVersionCode
                analyticsLogger.logEvent(
                    event = "downloaded",
                    params = mapOf("version_code" to downloadedVersionCode),
                )
            }

            UpdateInstallStatus.INSTALLED -> clearDownloadedState()
            else -> Unit
        }
        syncUiState()

        when (val decision = updatePolicyEngine.evaluate(snapshot, currentAppState)) {
            UpdateDecision.None -> {
                if (
                    snapshot.availability == UpdateAvailabilityStatus.UPDATE_AVAILABLE &&
                    currentAppState == UpdateAppState.CRITICAL
                ) {
                    analyticsLogger.logEvent(
                        event = "deferred_for_critical_flow",
                        params = mapOf("version_code" to snapshot.availableVersionCode),
                    )
                }
            }

            is UpdateDecision.Start -> maybeStartUpdate(
                launcher = launcher,
                updateType = decision.updateType,
                resumeInProgress = decision.resumeInProgress,
                versionCode = snapshot.availableVersionCode ?: lastKnownVersionCode,
            )
        }
    }

    fun onActivityResult(resultCode: Int) {
        val request = lastLaunchedRequest ?: return
        analyticsLogger.logEvent(
            event = "flow_result",
            params = mapOf(
                "result_code" to resultCode,
                "update_type" to request.updateType.name.lowercase(),
                "version_code" to request.versionCode,
            ),
        )
        if (request.updateType == UpdateType.IMMEDIATE && resultCode != Activity.RESULT_OK) {
            lastImmediateDeclinedVersionCode = request.versionCode
            lastImmediateDeclinedAtMs = updateClock.elapsedRealtime()
        }
        lastLaunchedRequest = null
    }

    fun acknowledgeRestartPrompt() {
        val versionCode = downloadedVersionCode ?: return
        acknowledgedRestartPromptVersions += versionCode
        syncUiState()
    }

    fun completeUpdate() {
        analyticsLogger.logEvent(
            event = "complete_requested",
            params = mapOf("version_code" to downloadedVersionCode),
        )
        updateDataSource.completeUpdate()
    }

    private suspend fun maybeStartUpdate(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: UpdateType,
        resumeInProgress: Boolean,
        versionCode: Int?,
    ) {
        val resolvedVersionCode = versionCode ?: return
        if (!resumeInProgress && shouldSkipDuplicatePrompt(resolvedVersionCode, updateType)) {
            analyticsLogger.logEvent(
                event = "duplicate_prompt_skipped",
                params = mapOf(
                    "update_type" to updateType.name.lowercase(),
                    "version_code" to resolvedVersionCode,
                ),
            )
            return
        }

        val started = runCatching {
            updateDataSource.startUpdateFlow(
                launcher = launcher,
                updateType = updateType,
            )
        }.onFailure { error ->
            analyticsLogger.logNonFatal(
                tag = "start_failed",
                throwable = error,
                extras = mapOf(
                    "update_type" to updateType.name.lowercase(),
                    "version_code" to resolvedVersionCode.toString(),
                ),
            )
        }.getOrDefault(false)

        if (!started) {
            return
        }

        lastLaunchedRequest = UpdatePromptRecord(
            versionCode = resolvedVersionCode,
            updateType = updateType,
        )
        if (updateType == UpdateType.FLEXIBLE) {
            promptedFlexibleVersions += resolvedVersionCode
        }
        analyticsLogger.logEvent(
            event = if (resumeInProgress) "resume_started" else "flow_started",
            params = mapOf(
                "update_type" to updateType.name.lowercase(),
                "version_code" to resolvedVersionCode,
            ),
        )
    }

    private fun shouldSkipDuplicatePrompt(versionCode: Int, updateType: UpdateType): Boolean {
        return when (updateType) {
            UpdateType.FLEXIBLE -> versionCode in promptedFlexibleVersions
            UpdateType.IMMEDIATE -> {
                lastImmediateDeclinedVersionCode == versionCode &&
                    updateClock.elapsedRealtime() - lastImmediateDeclinedAtMs <
                    updatePolicyEngine.immediateRetryCooldownMillis()
            }
        }
    }

    private fun onInstallStateChanged(state: UpdateInstallState) {
        currentInstallStatus = state.installStatus
        when (state.installStatus) {
            UpdateInstallStatus.DOWNLOADED -> {
                downloadedVersionCode = lastKnownVersionCode
                analyticsLogger.logEvent(
                    event = "download_listener_downloaded",
                    params = mapOf(
                        "version_code" to downloadedVersionCode,
                        "bytes_downloaded" to state.bytesDownloaded,
                        "total_bytes" to state.totalBytesToDownload,
                    ),
                )
            }

            UpdateInstallStatus.INSTALLED,
            UpdateInstallStatus.CANCELED,
            UpdateInstallStatus.FAILED -> clearDownloadedState()

            else -> Unit
        }
        syncUiState()
    }

    private fun clearDownloadedState() {
        downloadedVersionCode = null
        currentInstallStatus = UpdateInstallStatus.UNKNOWN
    }

    private fun syncUiState() {
        val versionCode = downloadedVersionCode
        val showRestartPrompt = versionCode != null &&
            currentAppState == UpdateAppState.SAFE &&
            versionCode !in acknowledgedRestartPromptVersions

        _uiState.value = UpdateUiState(
            appState = currentAppState,
            showRestartPrompt = showRestartPrompt,
            downloadedVersionCode = versionCode,
            installStatus = currentInstallStatus,
        )
    }

    private data class UpdatePromptRecord(
        val versionCode: Int,
        val updateType: UpdateType,
    )
}

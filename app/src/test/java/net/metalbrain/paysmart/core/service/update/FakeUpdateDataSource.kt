package net.metalbrain.paysmart.core.service.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat

class FakeUpdateDataSource(
    var currentSnapshot: UpdateInfoSnapshot = UpdateInfoSnapshot(),
    var startResult: Boolean = true,
) : UpdateDataSource {
    val startRequests = mutableListOf<UpdateType>()
    var completeCalled = false
    var listenerRegistered = false
        private set

    private var listener: ((UpdateInstallState) -> Unit)? = null

    override suspend fun getUpdateInfo(): UpdateInfoSnapshot = currentSnapshot

    override suspend fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        updateType: UpdateType,
    ): Boolean {
        startRequests += updateType
        return startResult
    }

    override fun registerListener(listener: (UpdateInstallState) -> Unit) {
        listenerRegistered = true
        this.listener = listener
    }

    override fun unregisterListener() {
        listenerRegistered = false
        listener = null
    }

    override fun completeUpdate() {
        completeCalled = true
    }

    fun dispatchInstallState(state: UpdateInstallState) {
        listener?.invoke(state)
    }
}

class FakeUpdateAnalyticsLogger : UpdateAnalyticsLogger {
    val events = mutableListOf<Pair<String, Map<String, Any?>>>()
    val nonFatals = mutableListOf<Pair<String, Map<String, String>>>()

    override fun logEvent(event: String, params: Map<String, Any?>) {
        events += event to params
    }

    override fun logNonFatal(
        tag: String,
        throwable: Throwable,
        extras: Map<String, String>,
    ) {
        nonFatals += tag to extras
    }
}

class StaticUpdatePolicyConfigProvider(
    private val config: UpdatePolicyConfig = UpdatePolicyConfig(),
) : UpdatePolicyConfigProvider {
    override fun getConfig(): UpdatePolicyConfig = config
}

class FakeUpdateClock(
    var nowMs: Long = 0L,
) : UpdateClock {
    override fun elapsedRealtime(): Long = nowMs
}

class TestIntentSenderLauncher : ActivityResultLauncher<IntentSenderRequest>() {
    override val contract = ActivityResultContracts.StartIntentSenderForResult()

    override fun unregister() = Unit

    override fun launch(
        input: IntentSenderRequest,
        options: ActivityOptionsCompat?,
    ) = Unit
}

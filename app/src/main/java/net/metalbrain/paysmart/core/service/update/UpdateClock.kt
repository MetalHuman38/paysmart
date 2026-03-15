package net.metalbrain.paysmart.core.service.update

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

interface UpdateClock {
    fun elapsedRealtime(): Long
}

@Singleton
class SystemUpdateClock @Inject constructor() : UpdateClock {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}

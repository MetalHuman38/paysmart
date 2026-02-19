package net.metalbrain.paysmart.utils

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicLong

class ThrottledLogger(
    private val tag: String,
    private val minIntervalMs: Long
) {
    private val lastLogTime = AtomicLong(0L)

    fun log(level: Int, message: String, throwable: Throwable? = null) {
        val now = SystemClock.elapsedRealtime()
        val last = lastLogTime.get()

        if (now - last >= minIntervalMs &&
            lastLogTime.compareAndSet(last, now)
        ) {
            when (level) {
                Log.WARN -> Log.w(tag, message, throwable)
                Log.ERROR -> Log.e(tag, message, throwable)
                else -> Log.d(tag, message, throwable)
            }
        }
    }
}

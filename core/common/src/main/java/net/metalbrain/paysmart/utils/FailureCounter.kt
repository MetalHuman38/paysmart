package net.metalbrain.paysmart.utils


class FailureCounter(private val maxRetries: Int = 3) {
    private var count = 0

    fun increment(): Boolean {
        count++
        return count >= maxRetries
    }

    fun reset() {
        count = 0
    }
}

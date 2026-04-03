package net.metalbrain.paysmart.core.service.performance

interface AppPerformanceMonitor {
    suspend fun <T> trace(
        name: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend () -> T
    ): T
}

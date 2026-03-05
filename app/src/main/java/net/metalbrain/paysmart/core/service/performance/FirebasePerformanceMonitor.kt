package net.metalbrain.paysmart.core.service.performance

import com.google.firebase.perf.FirebasePerformance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePerformanceMonitor @Inject constructor() : AppPerformanceMonitor {

    override suspend fun <T> trace(
        name: String,
        attributes: Map<String, String>,
        block: suspend () -> T
    ): T {
        val performance = FirebasePerformance.getInstance()
        if (!performance.isPerformanceCollectionEnabled) {
            return block()
        }

        val trace = performance.newTrace(sanitizeTraceName(name))
        attributes.forEach { (key, value) ->
            val safeKey = sanitizeAttributeKey(key)
            val safeValue = value.trim().take(MAX_ATTRIBUTE_VALUE)
            if (safeKey.isNotEmpty() && safeValue.isNotEmpty()) {
                runCatching { trace.putAttribute(safeKey, safeValue) }
            }
        }

        return try {
            trace.start()
            block()
        } catch (error: Throwable) {
            runCatching {
                trace.putMetric("error_count", 1L)
                trace.putAttribute("error_type", error.javaClass.simpleName.take(MAX_ATTRIBUTE_VALUE))
            }
            throw error
        } finally {
            runCatching { trace.stop() }
        }
    }

    private fun sanitizeTraceName(raw: String): String {
        val normalized = raw
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return normalized.take(MAX_TRACE_NAME).ifEmpty { "paysmart_trace" }
    }

    private fun sanitizeAttributeKey(raw: String): String {
        return raw
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(MAX_ATTRIBUTE_KEY)
    }
}

private const val MAX_TRACE_NAME = 100
private const val MAX_ATTRIBUTE_KEY = 40
private const val MAX_ATTRIBUTE_VALUE = 100

package net.metalbrain.paysmart.core.service.update

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigUpdatePolicyConfigProvider @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : UpdatePolicyConfigProvider {

    init {
        remoteConfig.setDefaultsAsync(DEFAULT_VALUES)
    }

    override fun getConfig(): UpdatePolicyConfig {
        return UpdatePolicyConfig(
            enabled = remoteConfig.getBoolean(KEY_ENABLED),
            flexibleMinStalenessDays = remoteConfig.getLong(KEY_FLEXIBLE_MIN_STALENESS_DAYS)
                .toInt()
                .coerceAtLeast(0),
            flexibleMinPriority = remoteConfig.getLong(KEY_FLEXIBLE_MIN_PRIORITY)
                .toInt()
                .coerceIn(0, 5),
            immediateMinStalenessDays = remoteConfig.getLong(KEY_IMMEDIATE_MIN_STALENESS_DAYS)
                .toInt()
                .coerceAtLeast(0),
            immediateMinPriority = remoteConfig.getLong(KEY_IMMEDIATE_MIN_PRIORITY)
                .toInt()
                .coerceIn(0, 5),
            immediateRetryCooldownMinutes = remoteConfig.getLong(KEY_IMMEDIATE_RETRY_COOLDOWN_MINUTES)
                .coerceAtLeast(1L),
        )
    }

    companion object {
        const val KEY_ENABLED = "app_update_enabled"
        const val KEY_FLEXIBLE_MIN_STALENESS_DAYS = "app_update_flexible_min_staleness_days"
        const val KEY_FLEXIBLE_MIN_PRIORITY = "app_update_flexible_min_priority"
        const val KEY_IMMEDIATE_MIN_STALENESS_DAYS = "app_update_immediate_min_staleness_days"
        const val KEY_IMMEDIATE_MIN_PRIORITY = "app_update_immediate_min_priority"
        const val KEY_IMMEDIATE_RETRY_COOLDOWN_MINUTES = "app_update_immediate_retry_cooldown_minutes"

        val DEFAULT_VALUES: Map<String, Any> = mapOf(
            KEY_ENABLED to true,
            KEY_FLEXIBLE_MIN_STALENESS_DAYS to 2L,
            KEY_FLEXIBLE_MIN_PRIORITY to 0L,
            KEY_IMMEDIATE_MIN_STALENESS_DAYS to 7L,
            KEY_IMMEDIATE_MIN_PRIORITY to 4L,
            KEY_IMMEDIATE_RETRY_COOLDOWN_MINUTES to 15L,
        )
    }
}

package net.metalbrain.paysmart.core.security

/**
 * Runtime migration toggles for staged rollout.
 * PR2: Room-authoritative reads are enabled by default behind a deterministic rollout gate.
 */
object SecurityMigrationFlags {
    @Volatile
    var roomAuthoritativeEnabled: Boolean = true

    @Volatile
    var roomAuthoritativeRolloutPercent: Int = 100

    @Volatile
    var roomAuthoritativeAllowlist: Set<String> = emptySet()

    @Volatile
    var legacyDatastoreMirrorEnabled: Boolean = true

    fun shouldUseRoomAuthoritative(userId: String): Boolean {
        if (!roomAuthoritativeEnabled) {
            return false
        }

        if (roomAuthoritativeAllowlist.contains(userId)) {
            return true
        }

        val percent = roomAuthoritativeRolloutPercent.coerceIn(0, 100)
        if (percent == 100) {
            return true
        }
        if (percent == 0) {
            return false
        }

        return stableBucketFor(userId) < percent
    }

    fun stableBucketFor(userId: String): Int {
        return (userId.hashCode() and Int.MAX_VALUE) % 100
    }
}

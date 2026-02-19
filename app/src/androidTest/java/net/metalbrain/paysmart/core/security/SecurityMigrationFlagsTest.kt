package net.metalbrain.paysmart.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityMigrationFlagsTest {

    @Test
    fun shouldUseRoomAuthoritative_defaultState_enablesRoomReads() {
        withFlags(
            enabled = true,
            percent = 100,
            allowlist = emptySet()
        ) {
            assertTrue(SecurityMigrationFlags.shouldUseRoomAuthoritative("uid-default"))
        }
    }

    @Test
    fun shouldUseRoomAuthoritative_zeroPercent_usesAllowlistOnly() {
        withFlags(
            enabled = true,
            percent = 0,
            allowlist = setOf("uid-allowlisted")
        ) {
            assertTrue(SecurityMigrationFlags.shouldUseRoomAuthoritative("uid-allowlisted"))
            assertFalse(SecurityMigrationFlags.shouldUseRoomAuthoritative("uid-not-allowlisted"))
        }
    }

    @Test
    fun stableBucketFor_isDeterministicForSameUid() {
        val a = SecurityMigrationFlags.stableBucketFor("uid-stable-123")
        val b = SecurityMigrationFlags.stableBucketFor("uid-stable-123")

        assertEquals(a, b)
        assertTrue(a in 0..99)
    }

    private inline fun withFlags(
        enabled: Boolean,
        percent: Int,
        allowlist: Set<String>,
        block: () -> Unit
    ) {
        val previousEnabled = SecurityMigrationFlags.roomAuthoritativeEnabled
        val previousPercent = SecurityMigrationFlags.roomAuthoritativeRolloutPercent
        val previousAllowlist = SecurityMigrationFlags.roomAuthoritativeAllowlist
        try {
            SecurityMigrationFlags.roomAuthoritativeEnabled = enabled
            SecurityMigrationFlags.roomAuthoritativeRolloutPercent = percent
            SecurityMigrationFlags.roomAuthoritativeAllowlist = allowlist
            block()
        } finally {
            SecurityMigrationFlags.roomAuthoritativeEnabled = previousEnabled
            SecurityMigrationFlags.roomAuthoritativeRolloutPercent = previousPercent
            SecurityMigrationFlags.roomAuthoritativeAllowlist = previousAllowlist
        }
    }
}

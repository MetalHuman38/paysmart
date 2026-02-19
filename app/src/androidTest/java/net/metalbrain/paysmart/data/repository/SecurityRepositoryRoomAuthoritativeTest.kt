package net.metalbrain.paysmart.data.repository

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import net.metalbrain.paysmart.core.security.SecurityMigrationFlags
import net.metalbrain.paysmart.core.security.SecurityPreference
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import net.metalbrain.paysmart.domain.room.RoomUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SecurityRepositoryRoomAuthoritativeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: SecurityRepository

    @Inject
    lateinit var roomUseCase: RoomUseCase

    @Inject
    lateinit var securityPreference: SecurityPreference

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
        securityPreference.clear()
    }

    @Test
    fun getLocalSettings_readsFromRoom_whenRolloutEnabledForUser() = runBlocking {
        val userId = "room-rollout-test-uid"
        val previousEnabled = SecurityMigrationFlags.roomAuthoritativeEnabled
        val previousPercent = SecurityMigrationFlags.roomAuthoritativeRolloutPercent
        val previousAllowlist = SecurityMigrationFlags.roomAuthoritativeAllowlist

        try {
            SecurityMigrationFlags.roomAuthoritativeEnabled = true
            SecurityMigrationFlags.roomAuthoritativeRolloutPercent = 0
            SecurityMigrationFlags.roomAuthoritativeAllowlist = setOf(userId)

            securityPreference.saveLocalSecurityState(
                LocalSecuritySettingsModel(
                    passcodeEnabled = false,
                    passwordEnabled = false,
                    biometricsRequired = true,
                    hasVerifiedEmail = false,
                    lockAfterMinutes = 20,
                    sessionLocked = true
                )
            )

            roomUseCase.saveSecuritySettings(
                userId = userId,
                model = SecuritySettingsModel(
                    passcodeEnabled = true,
                    passwordEnabled = true,
                    biometricsRequired = false,
                    hasVerifiedEmail = true,
                    lockAfterMinutes = 5
                )
            )

            val result = repository.getLocalSettings(userId)
            assertTrue(result.isSuccess)

            val settings = result.getOrNull()
            assertNotNull(settings)
            assertTrue(settings!!.passcodeEnabled)
            assertTrue(settings.passwordEnabled)
            assertFalse(settings.biometricsRequired)
            assertTrue(settings.hasVerifiedEmail)
            assertEquals(5, settings.lockAfterMinutes)
            assertTrue(settings.sessionLocked)
        } finally {
            SecurityMigrationFlags.roomAuthoritativeEnabled = previousEnabled
            SecurityMigrationFlags.roomAuthoritativeRolloutPercent = previousPercent
            SecurityMigrationFlags.roomAuthoritativeAllowlist = previousAllowlist
            securityPreference.clear()
        }
    }
}

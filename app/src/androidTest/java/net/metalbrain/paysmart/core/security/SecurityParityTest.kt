package net.metalbrain.paysmart.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityParityTest {

    @Test
    fun assertServerRoomParity_whenSnapshotsMatch_returnsMatch() {
        val server = SecuritySettingsModel(
            allowFederatedLinking = true,
            passcodeEnabled = true,
            passwordEnabled = true,
            biometricsRequired = false,
            biometricsEnabled = true,
            hasVerifiedEmail = true,
            lockAfterMinutes = 5
        )
        val room = server.copy()

        val result = SecurityParity.assertServerRoomParity(server, room)

        assertTrue(result.matches)
        assertTrue(result.mismatches.isEmpty())
    }

    @Test
    fun assertServerRoomParity_whenRoomDrifts_reportsMismatch() {
        val server = SecuritySettingsModel(
            passcodeEnabled = true,
            passwordEnabled = true
        )
        val room = SecuritySettingsModel(
            passcodeEnabled = true,
            passwordEnabled = false
        )

        val result = SecurityParity.assertServerRoomParity(server, room)

        assertFalse(result.matches)
        assertTrue(result.mismatches.contains(SecurityParityField.PASSWORD_ENABLED))
    }

    @Test
    fun assertRoomLocalParity_detectsAccountFlagDrift() {
        val room = SecuritySettingsModel(
            passcodeEnabled = false,
            passwordEnabled = false,
            biometricsEnabled = false,
            biometricsRequired = false,
            hasVerifiedEmail = true,
            lockAfterMinutes = 10
        )
        val local = LocalSecuritySettingsModel(
            passcodeEnabled = true,
            passwordEnabled = true,
            biometricsEnabled = true,
            biometricsRequired = false,
            hasVerifiedEmail = true,
            lockAfterMinutes = 10
        )

        val result = SecurityParity.assertRoomLocalParity(
            room = room,
            local = local,
            ignoreFields = SecurityParity.STICKY_LOCAL_FIELDS
        )

        assertFalse(result.matches)
        assertTrue(result.mismatches.contains(SecurityParityField.PASSCODE_ENABLED))
        assertTrue(result.mismatches.contains(SecurityParityField.PASSWORD_ENABLED))
        assertTrue(result.mismatches.contains(SecurityParityField.BIOMETRICS_ENABLED))
    }

    @Test
    fun assertRoomLocalParity_detectsNonStickyMismatch() {
        val room = SecuritySettingsModel(
            biometricsRequired = false,
            hasVerifiedEmail = true
        )
        val local = LocalSecuritySettingsModel(
            biometricsRequired = true,
            hasVerifiedEmail = true
        )

        val result = SecurityParity.assertRoomLocalParity(
            room = room,
            local = local,
            ignoreFields = SecurityParity.STICKY_LOCAL_FIELDS
        )

        assertFalse(result.matches)
        assertTrue(result.mismatches.contains(SecurityParityField.BIOMETRICS_REQUIRED))
    }
}

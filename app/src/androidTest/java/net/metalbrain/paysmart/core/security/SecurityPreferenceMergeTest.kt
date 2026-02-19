package net.metalbrain.paysmart.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityPreferenceMergeTest {

    @Test
    fun mergeServerWithLocal_appliesServerDisableFlags() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = SecurityPreference(context)

        val local = LocalSecuritySettingsModel(
            passcodeEnabled = true,
            passwordEnabled = true,
            biometricsRequired = true,
            lockAfterMinutes = 5
        )
        val server = SecuritySettingsModel(
            passcodeEnabled = false,
            passwordEnabled = false,
            biometricsRequired = false,
            lockAfterMinutes = 20
        )

        val merged = prefs.mergeServerWithLocal(server, local)

        assertFalse(merged.passcodeEnabled)
        assertFalse(merged.passwordEnabled)
        assertFalse(merged.biometricsRequired)
        assertEquals(20, merged.lockAfterMinutes)
    }

    @Test
    fun mergeServerWithLocal_appliesServerTrueFlags() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = SecurityPreference(context)

        val local = LocalSecuritySettingsModel(
            passcodeEnabled = false,
            passwordEnabled = false
        )
        val server = SecuritySettingsModel(
            passcodeEnabled = true,
            passwordEnabled = true
        )

        val merged = prefs.mergeServerWithLocal(server, local)

        assertTrue(merged.passcodeEnabled)
        assertTrue(merged.passwordEnabled)
    }
}

package net.metalbrain.paysmart.ui.capabilities

import com.google.firebase.Timestamp
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureRequirement
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [FeatureAccessPolicy].
 *
 * Verifies that feature access decisions are correctly evaluated based on the user's
 * security configuration, ensuring that sensitive features are blocked or allowed
 * according to the required security strength.
 */
class FeatureAccessPolicyTest {

    @Test
    fun `create invoice is blocked when only one security method is enabled`() {
        val settings = LocalSecuritySettingsModel(
            passwordEnabled = true,
            localPasswordSetAt = Timestamp(1, 0)
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.CREATE_INVOICE,
            settings = settings
        )

        assertFalse(decision.isAllowed)
        assertEquals(1, decision.currentSecurityStrength)
        assertEquals(
            listOf(FeatureRequirement.SECURITY_STRENGTH_TWO),
            decision.missingRequirements
        )
    }

    @Test
    fun `create invoice is allowed when two security methods are enabled`() {
        val settings = LocalSecuritySettingsModel(
            passcodeEnabled = true,
            localPassCodeSetAt = Timestamp(1, 0),
            biometricsEnabled = true
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.CREATE_INVOICE,
            settings = settings
        )

        assertTrue(decision.isAllowed)
        assertEquals(2, decision.currentSecurityStrength)
        assertEquals(2, decision.requiredSecurityStrength)
    }

    @Test
    fun `receive money is blocked until email and address are complete`() {
        val settings = LocalSecuritySettingsModel(
            hasVerifiedEmail = true,
            hasAddedHomeAddress = false
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.RECEIVE_MONEY,
            settings = settings
        )

        assertFalse(decision.isAllowed)
        assertEquals(
            listOf(FeatureRequirement.HOME_ADDRESS_VERIFIED),
            decision.missingRequirements
        )
    }

    @Test
    fun `receive money is allowed when email and address are complete`() {
        val settings = LocalSecuritySettingsModel(
            hasVerifiedEmail = true,
            hasAddedHomeAddress = true
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.RECEIVE_MONEY,
            settings = settings
        )

        assertTrue(decision.isAllowed)
        assertTrue(decision.missingRequirements.isEmpty())
    }
}

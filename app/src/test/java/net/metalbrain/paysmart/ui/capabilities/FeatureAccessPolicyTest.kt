package net.metalbrain.paysmart.ui.capabilities

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
 * security configuration, ensuring that features are blocked or allowed according
 * to their current product requirements.
 */
class FeatureAccessPolicyTest {

    @Test
    fun `create invoice is blocked until email and address are complete`() {
        val settings = LocalSecuritySettingsModel(
            hasVerifiedEmail = true,
            hasAddedHomeAddress = false,
            hasVerifiedIdentity = false
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.CREATE_INVOICE,
            settings = settings
        )

        assertFalse(decision.isAllowed)
        assertEquals(
            listOf(FeatureRequirement.HOME_ADDRESS_VERIFIED),
            decision.missingRequirements
        )
    }

    @Test
    fun `create invoice is allowed with email and address without identity`() {
        val settings = LocalSecuritySettingsModel(
            hasVerifiedEmail = true,
            hasAddedHomeAddress = true,
            hasVerifiedIdentity = false
        )

        val decision = FeatureAccessPolicy.evaluate(
            feature = FeatureKey.CREATE_INVOICE,
            settings = settings
        )

        assertTrue(decision.isAllowed)
        assertTrue(decision.missingRequirements.isEmpty())
        assertEquals(null, decision.requiredSecurityStrength)
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

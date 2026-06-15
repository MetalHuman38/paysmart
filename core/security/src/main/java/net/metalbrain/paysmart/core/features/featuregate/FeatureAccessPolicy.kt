package net.metalbrain.paysmart.core.features.featuregate

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

object FeatureAccessPolicy {
    private val requirementsByFeature: Map<FeatureKey, List<FeatureRequirement>> = mapOf(
        FeatureKey.ADD_MONEY to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED
        ),
        FeatureKey.RECEIVE_MONEY to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED
        ),
        FeatureKey.SEND_MONEY to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED,
            FeatureRequirement.IDENTITY_VERIFIED
        ),
        FeatureKey.CREATE_INVOICE to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED
        )
    )

    fun evaluate(
        feature: FeatureKey,
        settings: LocalSecuritySettingsModel?
    ): FeatureGateDecision {
        val securityStrengthScore = settings.securityStrengthScore()
        val requirements = requirementsByFeature[feature].orEmpty()
        val missing = requirements.filterNot { requirement ->
            isRequirementSatisfied(requirement, settings)
        }
        return FeatureGateDecision(
            feature = feature,
            missingRequirements = missing,
            currentSecurityStrength = securityStrengthScore.level
        )
    }

    private fun isRequirementSatisfied(
        requirement: FeatureRequirement,
        settings: LocalSecuritySettingsModel?
    ): Boolean {
        return when (requirement) {
            FeatureRequirement.VERIFIED_EMAIL -> settings?.hasVerifiedEmail == true
            FeatureRequirement.HOME_ADDRESS_VERIFIED -> settings?.hasAddedHomeAddress == true
            FeatureRequirement.IDENTITY_VERIFIED -> settings?.hasVerifiedIdentity == true
            FeatureRequirement.SECURITY_STRENGTH_TWO -> settings.securityStrengthScore().meets(2)
        }
    }

}

fun LocalSecuritySettingsModel?.securityStrengthScore(): SecurityStrengthScore {
    if (this == null) {
        return SecurityStrengthScore(emptySet())
    }

    val methods = buildSet {
        if (passwordEnabled && localPasswordSetAt != null) {
            add(SecurityStrengthMethod.PASSWORD)
        }
        if (passcodeEnabled && localPassCodeSetAt != null) {
            add(SecurityStrengthMethod.PASSCODE)
        }
        if (biometricsEnabled) {
            add(SecurityStrengthMethod.BIOMETRIC)
        }
        if (passkeyEnabled) {
            add(SecurityStrengthMethod.PASSKEY)
        }
    }

    return SecurityStrengthScore(methods)
}

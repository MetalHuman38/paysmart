package net.metalbrain.paysmart.ui.featuregate

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

enum class FeatureKey(val id: String) {
    ADD_MONEY("add_money"),
    SEND_MONEY("send_money");

    companion object {
        fun fromId(raw: String?): FeatureKey {
            return entries.firstOrNull { it.id == raw } ?: ADD_MONEY
        }
    }
}

enum class FeatureRequirement {
    VERIFIED_EMAIL,
    HOME_ADDRESS_VERIFIED,
    IDENTITY_VERIFIED
}

data class FeatureGateDecision(
    val feature: FeatureKey,
    val missingRequirements: List<FeatureRequirement>
) {
    val isAllowed: Boolean
        get() = missingRequirements.isEmpty()

    val nextRequirement: FeatureRequirement?
        get() = missingRequirements.firstOrNull()
}

object FeatureAccessPolicy {
    private val requirementsByFeature: Map<FeatureKey, List<FeatureRequirement>> = mapOf(
        FeatureKey.ADD_MONEY to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED
        ),
        FeatureKey.SEND_MONEY to listOf(
            FeatureRequirement.VERIFIED_EMAIL,
            FeatureRequirement.HOME_ADDRESS_VERIFIED,
            FeatureRequirement.IDENTITY_VERIFIED
        )
    )

    fun evaluate(
        feature: FeatureKey,
        settings: LocalSecuritySettingsModel?
    ): FeatureGateDecision {
        val requirements = requirementsByFeature[feature].orEmpty()
        val missing = requirements.filterNot { requirement ->
            isRequirementSatisfied(requirement, settings)
        }
        return FeatureGateDecision(feature = feature, missingRequirements = missing)
    }

    private fun isRequirementSatisfied(
        requirement: FeatureRequirement,
        settings: LocalSecuritySettingsModel?
    ): Boolean {
        if (settings == null) return false
        return when (requirement) {
            FeatureRequirement.VERIFIED_EMAIL -> settings.hasVerifiedEmail
            FeatureRequirement.HOME_ADDRESS_VERIFIED -> settings.hasAddedHomeAddress == true
            FeatureRequirement.IDENTITY_VERIFIED -> settings.hasVerifiedIdentity
        }
    }
}

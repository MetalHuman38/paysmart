package net.metalbrain.paysmart.core.features.featuregate

data class FeatureGateDecision(
    val feature: FeatureKey,
    val missingRequirements: List<FeatureRequirement>,
    val currentSecurityStrength: Int = 0,
    val requiredSecurityStrength: Int? = null
) {
    val isAllowed: Boolean
        get() = missingRequirements.isEmpty()

    val nextRequirement: FeatureRequirement?
        get() = missingRequirements.firstOrNull()
}

package net.metalbrain.paysmart.core.features.featuregate

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

enum class FeatureKey(val id: String) {
    ADD_MONEY("add_money"),
    SEND_MONEY("send_money"),
    CREATE_INVOICE("create_invoice");

    companion object {
        fun fromId(raw: String?): FeatureKey {
            return entries.firstOrNull { it.id == raw } ?: ADD_MONEY
        }
    }
}

enum class FeatureRequirement {
    VERIFIED_EMAIL,
    HOME_ADDRESS_VERIFIED,
    IDENTITY_VERIFIED,
    SECURITY_STRENGTH_TWO
}

enum class SecurityStrengthMethod {
    PASSWORD,
    PASSCODE,
    BIOMETRIC,
    PASSKEY
}

data class SecurityStrengthScore(
    val methods: Set<SecurityStrengthMethod>
) {
    val level: Int
        get() = methods.size

    fun meets(minimumLevel: Int): Boolean = level >= minimumLevel
}

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
        ),
        FeatureKey.CREATE_INVOICE to listOf(
            FeatureRequirement.SECURITY_STRENGTH_TWO
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
            currentSecurityStrength = securityStrengthScore.level,
            requiredSecurityStrength = minimumSecurityStrengthFor(feature)
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

    private fun minimumSecurityStrengthFor(feature: FeatureKey): Int? {
        return when (feature) {
            FeatureKey.CREATE_INVOICE -> 2
            else -> null
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

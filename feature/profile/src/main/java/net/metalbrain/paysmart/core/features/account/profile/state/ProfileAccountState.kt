package net.metalbrain.paysmart.core.features.account.profile.state

import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

enum class ProfileMissingItem {
    FULL_NAME,
    DATE_OF_BIRTH,
    ADDRESS_LINE_1,
    CITY,
    EMAIL_ADDRESS,
    PHONE_NUMBER,
    COUNTRY,
    POSTAL_CODE,
    VERIFIED_EMAIL,
    HOME_ADDRESS_VERIFIED,
    IDENTITY_VERIFIED
}

enum class ProfileNextStep {
    VERIFY_EMAIL,
    COMPLETE_ADDRESS,
    VERIFY_IDENTITY,
    REVIEW_PROFILE
}

data class ProfileAccountState(
    val loading: Boolean = true,
    val authenticated: Boolean = false,
    val user: AuthUserModel? = null,
    val security: LocalSecuritySettingsModel? = null,
    val missingItems: List<ProfileMissingItem> = emptyList(),
    val nextStep: ProfileNextStep? = null,
    val isLocked: Boolean = false
) {
    val isIncomplete: Boolean
        get() = missingItems.isNotEmpty()
}

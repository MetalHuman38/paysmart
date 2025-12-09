package net.metalbrain.paysmart.domain.model

import java.security.Timestamp

enum class UserStatus {
    Unverified,
    Verified,
    KycPending;

    companion object {
        fun fromString(status: String?): UserStatus = when (status) {
            "verified" -> Verified
            "kycPending" -> KycPending
            else -> Unverified
        }
    }
}

data class AuthUserModel(
    val uid: String = "",
    val email: String? = null,
    val emailVerified: Boolean = false,
    val displayName: String? = null,
    val photoURL: String? = null,
    val phoneNumber: String? = null,
    val isAnonymous: Boolean = false,
    val tenantId: String? = null,
    val providerIds: List<String> = emptyList(),
    val status: UserStatus = UserStatus.Unverified,
    val hasVerifiedEmail: Boolean = false,
    val hasAddedHomeAddress: Boolean = false,
    val hasVerifiedIdentity: Boolean = false,
    val hasLocalPassword: Boolean = false,
    val localPasswordSetAt: Timestamp? = null


) {
    companion object {
        fun fromMap(map: Map<String, Any?>): AuthUserModel {
            return AuthUserModel(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String,
                emailVerified = map["emailVerified"] as? Boolean ?: false,
                displayName = map["displayName"] as? String,
                photoURL = map["photoURL"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                isAnonymous = map["isAnonymous"] as? Boolean ?: false,
                tenantId = map["tenantId"] as? String,
                providerIds = (map["providerIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                status = UserStatus.fromString(map["status"] as? String),
                hasVerifiedEmail = map["hasVerifiedEmail"] as? Boolean ?: false,
                hasAddedHomeAddress = map["hasAddedHomeAddress"] as? Boolean ?: false,
                hasVerifiedIdentity = map["hasVerifiedIdentity"] as? Boolean ?: false,
                hasLocalPassword = map["hasLocalPassword"] as? Boolean ?: false,
                localPasswordSetAt = map["localPasswordSetAt"] as? Timestamp
            )
        }
    }
}

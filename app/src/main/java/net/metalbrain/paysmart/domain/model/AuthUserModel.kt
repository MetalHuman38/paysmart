package net.metalbrain.paysmart.domain.model

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

enum class ProgressFlag {
    None,
    VerifiedEmail,
    AddedHomeAddress,
    VerifiedIdentity;

    companion object {
        fun fromString(flag: String?): ProgressFlag? = when (flag) {
            "verifiedEmail" -> VerifiedEmail
            "addedHomeAddress" -> AddedHomeAddress
            "verifiedIdentity" -> VerifiedIdentity
            else -> null
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
    val progressFlags: List<ProgressFlag> = emptyList()

) {
    companion object {
        fun fromMap(map: Map<String, Any?>): AuthUserModel {
            val flagsRaw = map["progressFlags"] as? List<*>

            val flags = flagsRaw
                ?.mapNotNull { ProgressFlag.fromString(it as? String) }
                ?: emptyList()

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
                // FINAL MIGRATED FLAGS
                progressFlags = flags
            )
        }
    }
}

val AuthUserModel.hasVerifiedEmail: Boolean
    get() = ProgressFlag.VerifiedEmail in progressFlags

val AuthUserModel.hasAddedHomeAddress: Boolean
    get() = ProgressFlag.AddedHomeAddress in progressFlags

val AuthUserModel.hasVerifiedIdentity: Boolean
    get() = ProgressFlag.VerifiedIdentity in progressFlags

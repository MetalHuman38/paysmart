package net.metalbrain.paysmart.domain.model
import androidx.annotation.Keep

@Keep
data class AuthUserModel(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoURL: String? = null,
    val phoneNumber: String? = null,
    val isAnonymous: Boolean = false,
    val tenantId: String? = null,
    val providerIds: List<String> = emptyList()
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): AuthUserModel {
            return AuthUserModel(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String,
                displayName = map["displayName"] as? String,
                photoURL = map["photoURL"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                isAnonymous = map["isAnonymous"] as? Boolean ?: false,
                tenantId = map["tenantId"] as? String,
                providerIds = (map["providerIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}

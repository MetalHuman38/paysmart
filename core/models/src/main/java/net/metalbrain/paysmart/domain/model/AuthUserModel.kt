package net.metalbrain.paysmart.domain.model

import androidx.annotation.Keep

@Keep
data class AuthUserModel(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoURL: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val launchInterest: LaunchInterest? = null,
    val isAnonymous: Boolean = false,
    val tenantId: String? = null,
    val providerIds: List<String> = emptyList()
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): AuthUserModel {
            val addressMap = map["address"] as? Map<*, *>

            fun readString(vararg keys: String): String? {
                keys.forEach { key ->
                    val value = map[key] as? String
                    if (!value.isNullOrBlank()) {
                        return value
                    }
                }
                return null
            }

            fun readAddressString(vararg keys: String): String? {
                keys.forEach { key ->
                    val value = addressMap?.get(key) as? String
                    if (!value.isNullOrBlank()) {
                        return value
                    }
                }
                return null
            }

            return AuthUserModel(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String,
                displayName = map["displayName"] as? String,
                photoURL = map["photoURL"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                dateOfBirth = readString("dateOfBirth", "dob"),
                addressLine1 = readString("addressLine1", "address1") ?: readAddressString("line1"),
                addressLine2 = readString("addressLine2", "address2") ?: readAddressString("line2"),
                city = readString("city") ?: readAddressString("city"),
                country = readString("country") ?: readAddressString("country"),
                postalCode = readString("postalCode", "zipCode", "zipcode") ?: readAddressString("postalCode", "zipCode", "zipcode"),
                launchInterest = LaunchInterest.fromRaw(map["launchInterest"] as? String),
                isAnonymous = map["isAnonymous"] as? Boolean ?: false,
                tenantId = map["tenantId"] as? String,
                providerIds = (map["providerIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}

fun AuthUserModel.hasCompleteAccountInformation(): Boolean {
    return !displayName.isNullOrBlank() &&
        !dateOfBirth.isNullOrBlank() &&
        !addressLine1.isNullOrBlank() &&
        !city.isNullOrBlank() &&
        !country.isNullOrBlank() &&
        !postalCode.isNullOrBlank() &&
        !email.isNullOrBlank() &&
        !phoneNumber.isNullOrBlank()
}

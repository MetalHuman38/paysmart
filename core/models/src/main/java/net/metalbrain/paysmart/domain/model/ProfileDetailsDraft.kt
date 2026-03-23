package net.metalbrain.paysmart.domain.model

data class ProfileDetailsDraft(
    val fullName: String? = null,
    val dateOfBirth: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
) {
    fun isComplete(): Boolean {
        return !fullName.isNullOrBlank() &&
            !dateOfBirth.isNullOrBlank() &&
            !addressLine1.isNullOrBlank() &&
            !city.isNullOrBlank() &&
            !country.isNullOrBlank() &&
            !postalCode.isNullOrBlank() &&
            !email.isNullOrBlank() &&
            !phoneNumber.isNullOrBlank()
    }
}

package net.metalbrain.paysmart.core.features.identity.data

data class ClientInformation(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val email: String,
    val dateOfBirth: String,
    val countryIso2: String
) {
    val fullName: String
        get() = listOf(firstName, middleName, lastName)
            .filter { !it.isNullOrBlank() }
            .joinToString(" ")
}

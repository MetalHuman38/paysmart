package net.metalbrain.paysmart.ui.profile.identity.provider

import net.metalbrain.paysmart.ui.profile.data.type.KycCountryDocuments
import net.metalbrain.paysmart.ui.profile.data.type.KycDocumentType
import java.util.Locale

enum class CameraFrameShape {
    PASSPORT,
    CARD,
    GENERIC
}


private val kycDocumentDatabase: List<KycCountryDocuments> = listOf(
    KycCountryDocuments(
        iso2 = "GB",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "brp", label = "Biometric residence permit"),
            KycDocumentType(id = "refugee", label = "Refuge identity document"),
            KycDocumentType(id = "national_id", label = "National ID", accepted = false),
            KycDocumentType(id = "citizen_card", label = "Citizen card", accepted = false),
            KycDocumentType(id = "visa", label = "Visa", accepted = false),
            KycDocumentType(id = "shotgun", label = "Shotgun license", accepted = false),
            KycDocumentType(
                id = "irish_reg",
                label = "Irish certificate of registration",
                accepted = false
            ),
        ),
    ),
    KycCountryDocuments(
        iso2 = "US",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "state_id", label = "State ID"),
            KycDocumentType(id = "green_card", label = "Green Card"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "NG",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(
                id = "nin",
                label = "National Identification Number (NIN) card",
            ),
            KycDocumentType(id = "voters", label = "Voter's card"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "GH",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "voters", label = "Voter's card"),
            KycDocumentType(id = "national_id", label = "National ID"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "ZA",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "national_id", label = "National ID"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "KE",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "nhif", label = "NHIF card"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "UG",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "TZ",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "DE",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "FR",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "IT",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "ES",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "national_id", label = "National ID"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "CA",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "state_id", label = "State ID"),
        ),
    ),
    KycCountryDocuments(
        iso2 = "AU",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "medicare", label = "Medicare card"),
        ),
    ),

    KycCountryDocuments(
        iso2 = "NZ",
        documents = listOf(
            KycDocumentType(id = "passport", label = "passport"),
            KycDocumentType(id = "drivers_license", label = "Driver's license"),
            KycDocumentType(id = "national_id", label = "National ID"),
        ),
    ),
)

object KycDocumentCatalog {
    private val countryToDocuments: Map<String, List<KycDocumentType>> =
        kycDocumentDatabase.associate { it.iso2.uppercase(Locale.US) to it.documents }

    val supportedCountriesIso2: List<String> = countryToDocuments.keys.sorted()

    fun resolveCountry(preferredIso2: String?): String {
        val normalized = preferredIso2
            ?.trim()
            ?.uppercase(Locale.US)
            .orEmpty()
        return if (countryToDocuments.containsKey(normalized)) normalized else "GB"
    }

    fun documentsForCountry(iso2: String): List<KycDocumentType> {
        return countryToDocuments[resolveCountry(iso2)].orEmpty()
    }
}

val KycDocumentType.formattedLabel: String
    get() {
        if (label.isBlank()) return label
        return label
            .replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                token.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase(Locale.US) else c.toString()
                }
            }
    }

val KycDocumentType.frameShape: CameraFrameShape
    get() = when (id.lowercase(Locale.US)) {
        "passport" -> CameraFrameShape.PASSPORT
        "drivers_license",
        "national_id",
        "state_id",
        "green_card",
        "citizen_card",
        "brp" -> CameraFrameShape.CARD

        else -> CameraFrameShape.GENERIC
    }

val KycDocumentType.captureLabel: String
    get() = when (id.lowercase(Locale.US)) {
        "passport" -> "Passport photo page"
        "drivers_license" -> "Driver's license front"
        "national_id" -> "National ID front"
        "state_id" -> "State ID front"
        "green_card" -> "Green Card front"
        "brp" -> "Residence permit front"
        else -> "$formattedLabel photo"
    }

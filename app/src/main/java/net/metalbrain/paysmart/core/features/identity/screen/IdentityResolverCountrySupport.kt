package net.metalbrain.paysmart.core.features.identity.screen

import android.content.Context
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycReviewWindow
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.identity.provider.KycDocumentCatalog
import java.util.Locale

data class IdentityCountryPresentation(
    val iso2: String,
    val name: String,
    val flag: String,
    val reviewWindowLabel: String
)

fun buildIdentityCountryPresentations(
    context: Context,
    countriesIso2: List<String>
): List<IdentityCountryPresentation> {
    return countriesIso2.map { iso2 ->
        resolveIdentityCountryPresentation(context, iso2)
    }
}

fun resolveIdentityCountryPresentation(
    context: Context,
    iso2: String
): IdentityCountryPresentation {
    val normalizedIso2 = KycDocumentCatalog.resolveCountry(iso2)
    val reviewWindowLabel = context.formatReviewWindow(
        KycDocumentCatalog.reviewWindowForCountry(normalizedIso2)
    )
    val catalogCountry = CountrySelectionCatalog.countryByIso2(context, normalizedIso2)
    if (catalogCountry != null) {
        return IdentityCountryPresentation(
            iso2 = catalogCountry.iso2,
            name = catalogCountry.name,
            flag = catalogCountry.flagEmoji,
            reviewWindowLabel = reviewWindowLabel
        )
    }
    return IdentityCountryPresentation(
        iso2 = normalizedIso2,
        name = displayCountryName(normalizedIso2),
        flag = iso2ToFlagEmoji(normalizedIso2),
        reviewWindowLabel = reviewWindowLabel
    )
}

fun Context.formatReviewWindow(reviewWindow: KycReviewWindow): String {
    return if (reviewWindow.minHours == reviewWindow.maxHours) {
        getString(R.string.identity_resolver_review_window_exact_hours, reviewWindow.minHours)
    } else {
        getString(
            R.string.identity_resolver_review_window_range_hours,
            reviewWindow.minHours,
            reviewWindow.maxHours
        )
    }
}

private fun displayCountryName(iso2: String): String {
    val locale = runCatching { Locale.Builder().setRegion(iso2).build() }.getOrNull()
    return locale?.getDisplayCountry(Locale.getDefault()).orEmpty().ifBlank { iso2 }
}

private fun iso2ToFlagEmoji(rawIso2: String): String {
    val iso2 = rawIso2.trim().uppercase(Locale.US)
    if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) return "🌍"
    val first = Character.codePointAt(iso2, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(iso2, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

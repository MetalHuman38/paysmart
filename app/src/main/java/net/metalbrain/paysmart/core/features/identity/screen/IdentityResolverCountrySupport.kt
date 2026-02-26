package net.metalbrain.paysmart.core.features.identity.screen

import android.content.Context
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.ui.components.CatalogSelectionOption
import java.util.Locale

data class IdentityCountryPresentation(
    val iso2: String,
    val name: String,
    val flag: String
)

fun buildIdentityCountryOptions(
    context: Context,
    countriesIso2: List<String>
): List<CatalogSelectionOption> {
    return countriesIso2.map { iso2 ->
        val country = resolveIdentityCountryPresentation(context, iso2)
        CatalogSelectionOption(
            key = country.iso2,
            title = country.name,
            subtitle = country.iso2,
            leadingEmoji = country.flag
        )
    }
}

fun resolveIdentityCountryPresentation(
    context: Context,
    iso2: String
): IdentityCountryPresentation {
    val catalogCountry = CountrySelectionCatalog.countryByIso2(context, iso2)
    if (catalogCountry != null) {
        return IdentityCountryPresentation(
            iso2 = catalogCountry.iso2,
            name = catalogCountry.name,
            flag = catalogCountry.flagEmoji
        )
    }
    return IdentityCountryPresentation(
        iso2 = iso2,
        name = displayCountryName(iso2),
        flag = iso2ToFlagEmoji(iso2)
    )
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

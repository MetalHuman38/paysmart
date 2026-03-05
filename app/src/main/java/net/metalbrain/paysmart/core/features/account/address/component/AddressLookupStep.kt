package net.metalbrain.paysmart.core.features.account.address.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.ui.components.CatalogSelectionBottomSheet
import net.metalbrain.paysmart.ui.components.CatalogSelectionOption
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import java.util.Locale

@Composable
fun AddressLookupStep(
    house: String,
    city: String,
    stateOrRegion: String,
    postcode: String,
    country: String,
    isLoading: Boolean,
    onHouseChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateOrRegionChanged: (String) -> Unit,
    onPostcodeChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onResolve: () -> Unit
) {
    val context = LocalContext.current
    var showCountrySheet by remember { mutableStateOf(false) }
    val iso2 = country.trim().uppercase(Locale.US).take(2)
    val selectedCountry = CountrySelectionCatalog.countryByIso2(context, iso2)
    val selectedFlagEmoji = CountrySelectionCatalog.flagForCountry(context, iso2)
    val countryOptions = remember(context) {
        CountrySelectionCatalog.countries(context).map { countryItem ->
            CatalogSelectionOption(
                key = countryItem.iso2,
                title = countryItem.name,
                subtitle = "${countryItem.iso2} • ${countryItem.currencyCode}",
                leadingEmoji = countryItem.flagEmoji
            )
        }
    }
    val countryDisplay = selectedCountry?.let {
        "${it.flagEmoji} ${it.name} (${it.iso2})"
    } ?: "$selectedFlagEmoji $iso2"

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.smallSpacing)
    ) {
        OutlinedTextField(
            value = house,
            onValueChange = onHouseChanged,
            label = { Text(stringResource(R.string.profile_field_address_line_1)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = city,
            onValueChange = onCityChanged,
            label = { Text(stringResource(R.string.profile_field_city)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stateOrRegion,
            onValueChange = onStateOrRegionChanged,
            label = { Text(stringResource(R.string.address_resolver_state_region_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = postcode,
            onValueChange = onPostcodeChanged,
            label = { Text(stringResource(R.string.profile_field_postal_code)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = countryDisplay,
            onValueChange = {},
            label = { Text(stringResource(R.string.select_country)) },
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { showCountrySheet = true },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) {
                    showCountrySheet = true
                }
        )

        PrimaryButton(
            text = stringResource(R.string.address_resolver_lookup_action),
            onClick = onResolve,
            enabled = !isLoading,
            isLoading = isLoading
        )
    }

    if (showCountrySheet) {
        CatalogSelectionBottomSheet(
            title = stringResource(R.string.select_country),
            options = countryOptions,
            selectedKey = iso2,
            onDismiss = { showCountrySheet = false },
            onSelect = { selected ->
                onCountryChanged(selected.key)
            }
        )
    }
}

package net.metalbrain.paysmart.ui.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun AddressLookupStep(
    house: String,
    postcode: String,
    country: String,
    isLoading: Boolean,
    onHouseChanged: (String) -> Unit,
    onPostcodeChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onResolve: () -> Unit
) {
    Text(
        text = stringResource(R.string.address_resolver_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = house,
        onValueChange = onHouseChanged,
        label = { Text(stringResource(R.string.address_resolver_house_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = postcode,
        onValueChange = onPostcodeChanged,
        label = { Text(stringResource(R.string.address_resolver_postcode_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = country,
        onValueChange = onCountryChanged,
        label = { Text(stringResource(R.string.address_resolver_country_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    PrimaryButton(
        text = stringResource(R.string.address_resolver_lookup_action),
        onClick = onResolve,
        enabled = !isLoading,
        isLoading = isLoading
    )
}

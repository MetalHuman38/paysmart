package net.metalbrain.paysmart.ui.profile.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton


@Composable
fun AddressFinalConfirmStep(
    line1: String,
    line2: String,
    city: String,
    stateOrRegion: String,
    postCode: String,
    countryCode: String,
    isSaving: Boolean,
    onLine1Changed: (String) -> Unit,
    onLine2Changed: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onStateOrRegionChanged: (String) -> Unit,
    onPostCodeChanged: (String) -> Unit,
    onCountryCodeChanged: (String) -> Unit,
    onBackToMap: () -> Unit,
    onConfirm: () -> Unit
) {
    Text(
        text = stringResource(R.string.address_resolver_final_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = line1,
        onValueChange = onLine1Changed,
        label = { Text(stringResource(R.string.address_resolver_line1_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = line2,
        onValueChange = onLine2Changed,
        label = { Text(stringResource(R.string.profile_field_address_line_2)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = city,
        onValueChange = onCityChanged,
        label = { Text(stringResource(R.string.address_resolver_city_label)) },
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
        value = postCode,
        onValueChange = onPostCodeChanged,
        label = { Text(stringResource(R.string.address_resolver_postcode_short_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = countryCode,
        onValueChange = onCountryCodeChanged,
        label = { Text(stringResource(R.string.address_resolver_country_code_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedButton(
        onClick = onBackToMap,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.address_resolver_edit_on_map_action))
    }

    PrimaryButton(
        text = stringResource(R.string.address_resolver_confirm_action),
        onClick = onConfirm,
        enabled = !isSaving,
        isLoading = isSaving
    )
}

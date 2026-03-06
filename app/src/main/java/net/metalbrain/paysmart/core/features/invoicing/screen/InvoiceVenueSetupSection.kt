package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun InvoiceVenueSetupSection(
    state: InvoiceSetupUiState,
    onVenueNameChanged: (String) -> Unit,
    onVenueAddressChanged: (String) -> Unit,
    onVenueCountryChanged: (String) -> Unit,
    onVenueRateChanged: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onApplySuggestedAddress: () -> Unit,
    onAddVenue: () -> Unit,
    onSelectVenue: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = stringResource(R.string.invoice_setup_venue_title))
            Text(text = stringResource(R.string.invoice_setup_venue_hint))
            if (state.venues.isNotEmpty()) {
                Text(text = stringResource(R.string.invoice_setup_existing_venues_title))
                state.venues.forEach { venue ->
                    FilterChip(
                        selected = state.effectiveSelectedVenueId == venue.venueId,
                        onClick = { onSelectVenue(venue.venueId) },
                        label = { Text(venue.venueName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            OutlinedTextField(
                value = state.venueNameInput,
                onValueChange = onVenueNameChanged,
                label = { Text(stringResource(R.string.invoice_setup_venue_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.venueAddressInput,
                onValueChange = onVenueAddressChanged,
                label = { Text(stringResource(R.string.invoice_setup_venue_address_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.venueCountryInput,
                onValueChange = onVenueCountryChanged,
                label = { Text(stringResource(R.string.invoice_setup_venue_country_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.venueRateInput,
                onValueChange = onVenueRateChanged,
                label = { Text(stringResource(R.string.invoice_setup_venue_rate_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedButton(
                onClick = onSearchAddress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.invoice_setup_search_address_action))
            }
            if (state.suggestedVenueAddress != null) {
                OutlinedButton(
                    onClick = onApplySuggestedAddress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.invoice_setup_apply_address_action))
                }
            }
            PrimaryButton(
                text = stringResource(R.string.invoice_setup_add_venue_action),
                onClick = onAddVenue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

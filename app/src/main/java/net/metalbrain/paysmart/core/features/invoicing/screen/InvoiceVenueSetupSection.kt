package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SecondaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

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
    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = stringResource(R.string.invoice_setup_venue_title),
            body = stringResource(R.string.invoice_setup_venue_hint)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            if (state.venues.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.invoice_setup_existing_venues_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.venues.forEach { venue ->
                    FilterChip(
                        selected = state.effectiveSelectedVenueId == venue.venueId,
                        onClick = { onSelectVenue(venue.venueId) },
                        label = { Text(venue.venueName) }
                    )
                }
            }
            InvoiceInputField(
                value = state.venueNameInput,
                onValueChange = onVenueNameChanged,
                label = stringResource(R.string.invoice_setup_venue_name_label)
            )
            InvoiceInputField(
                value = state.venueAddressInput,
                onValueChange = onVenueAddressChanged,
                label = stringResource(R.string.invoice_setup_venue_address_label),
                singleLine = false
            )
            InvoiceInputField(
                value = state.venueCountryInput,
                onValueChange = onVenueCountryChanged,
                label = stringResource(R.string.invoice_setup_venue_country_label)
            )
            InvoiceInputField(
                value = state.venueRateInput,
                onValueChange = onVenueRateChanged,
                label = stringResource(R.string.invoice_setup_venue_rate_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            SecondaryButton(
                text = stringResource(R.string.invoice_setup_search_address_action),
                onClick = onSearchAddress,
                enabled = !state.isAddressSearching
            )
            if (state.suggestedVenueAddress != null) {
                SecondaryButton(
                    text = stringResource(R.string.invoice_setup_apply_address_action),
                    onClick = onApplySuggestedAddress
                )
            }
            PrimaryButton(
                text = stringResource(R.string.invoice_setup_add_venue_action),
                onClick = onAddVenue
            )
        }
    }
}

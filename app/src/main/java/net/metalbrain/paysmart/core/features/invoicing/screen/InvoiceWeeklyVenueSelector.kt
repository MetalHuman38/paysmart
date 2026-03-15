package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState

@Composable
internal fun InvoiceWeeklyVenueSelector(
    state: InvoiceSetupUiState,
    onVenueSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.invoice_weekly_select_venue))
        if (state.venues.isEmpty()) {
            Text(text = stringResource(R.string.invoice_weekly_no_venues))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.venues.forEach { venue ->
                    FilterChip(
                        selected = state.effectiveSelectedVenueId == venue.venueId,
                        onClick = { onVenueSelected(venue.venueId) },
                        label = { Text(venue.venueName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

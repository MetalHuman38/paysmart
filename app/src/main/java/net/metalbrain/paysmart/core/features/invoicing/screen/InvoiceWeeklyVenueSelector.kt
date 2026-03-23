package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun InvoiceWeeklyVenueSelector(
    state: InvoiceSetupUiState,
    onVenueSelected: (String) -> Unit
) {
    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = stringResource(R.string.invoice_weekly_select_venue),
            body = if (state.venues.isEmpty()) {
                stringResource(R.string.invoice_weekly_no_venues)
            } else {
                null
            }
        )

        if (state.venues.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                state.venues.forEach { venue ->
                    FilterChip(
                        selected = state.effectiveSelectedVenueId == venue.venueId,
                        onClick = { onVenueSelected(venue.venueId) },
                        label = { Text(venue.venueName) }
                    )
                }
            }
        }
    }
}

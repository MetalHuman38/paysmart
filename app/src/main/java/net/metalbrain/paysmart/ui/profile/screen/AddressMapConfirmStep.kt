package net.metalbrain.paysmart.ui.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.profile.components.AddressResultLine


@Composable
fun AddressMapConfirmStep(
    fullAddress: String,
    line1: String,
    city: String?,
    postCode: String,
    countryCode: String,
    source: String?,
    lat: Double,
    lng: Double,
    onNoClick: () -> Unit,
    onYesClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.address_resolver_map_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        AddressMapPreview(
            lat = lat,
            lng = lng,
            markerTitle = fullAddress,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.address_resolver_result_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            AddressResultLine(
                label = stringResource(R.string.address_resolver_line1_label),
                value = line1
            )
            AddressResultLine(
                label = stringResource(R.string.address_resolver_city_label),
                value = city ?: "-"
            )
            AddressResultLine(
                label = stringResource(R.string.address_resolver_country_code_label),
                value = countryCode
            )
            AddressResultLine(
                label = stringResource(R.string.address_resolver_postcode_short_label),
                value = postCode
            )
            source?.let {
                AddressResultLine(
                    label = stringResource(R.string.address_resolver_source_label),
                    value = it
                )
            }
        }
    }

    Text(
        text = stringResource(R.string.address_resolver_six_months_question),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onNoClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(stringResource(R.string.address_resolver_no_action))
        }
        PrimaryButton(
            text = stringResource(R.string.address_resolver_yes_action),
            onClick = onYesClick,
            modifier = Modifier.weight(1f)
        )
    }
}

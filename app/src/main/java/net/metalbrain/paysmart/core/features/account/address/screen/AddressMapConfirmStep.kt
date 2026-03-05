package net.metalbrain.paysmart.core.features.account.address.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.address.component.AddressResultLine
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.OutlinedButton as AppOutlinedButton
import java.util.Locale


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
    val countryDisplay = "${iso2ToFlagEmoji(countryCode)} ${countryCode.uppercase(Locale.US)}"

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
                value = countryDisplay
            )
            AddressResultLine(
                label = stringResource(R.string.profile_field_postal_code),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppOutlinedButton(
            onClick = onNoClick,
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.address_resolver_no_action)
        )
        PrimaryButton(
            text = stringResource(R.string.address_resolver_yes_action),
            onClick = onYesClick,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun iso2ToFlagEmoji(rawIso2: String): String {
    val iso2 = rawIso2.trim().uppercase(Locale.US)
    if (iso2.length != 2 || iso2.any { it !in 'A'..'Z' }) {
        return CountryCapabilityCatalog.defaultProfile().flagEmoji
    }
    val first = Character.codePointAt(iso2, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(iso2, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

package net.metalbrain.paysmart.core.features.addmoney.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R

@Composable
internal fun AddMoneyAvailabilityCard(
    countryName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.add_money_unavailable_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.add_money_unavailable_message_format, countryName),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.add_money_unavailable_supporting_format, countryName),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

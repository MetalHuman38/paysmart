package net.metalbrain.paysmart.core.features.fundingaccount.card

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
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.util.providerLabel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountHeroCard(
    flagEmoji: String,
    currencyCode: String,
    countryName: String,
    provider: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Text(
                text = flagEmoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.funding_account_hero_title_format, currencyCode),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(
                    R.string.funding_account_hero_supporting_format,
                    currencyCode,
                    countryName
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FundingAccountMetaLine(
                label = stringResource(R.string.funding_account_hero_market_label),
                value = countryName
            )
            FundingAccountMetaLine(
                label = stringResource(R.string.funding_account_hero_provider_label),
                value = providerLabel(
                    rawProvider = provider,
                    flutterwaveLabel = stringResource(R.string.add_money_provider_flutterwave)
                )
            )
        }
    }
}

@Composable
internal fun FundingAccountMetaLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

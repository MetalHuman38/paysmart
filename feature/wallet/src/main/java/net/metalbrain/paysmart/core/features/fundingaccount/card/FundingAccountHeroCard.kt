package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.wallet.R
import net.metalbrain.paysmart.core.features.fundingaccount.util.providerLabel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountHeroCard(
    flagEmoji: String,
    currencyCode: String,
    countryName: String,
    provider: String?,
    isMarketSupported: Boolean,
    modifier: Modifier = Modifier
) {
    FundingAccountSurfaceCard(
        modifier = modifier,
        accentColor = MaterialTheme.colorScheme.primary,
        highlighted = isMarketSupported
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = flagEmoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(Dimens.sm)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = stringResource(R.string.funding_account_hero_title_format, currencyCode),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
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
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            FundingAccountMetaChip(
                label = stringResource(R.string.funding_account_hero_market_label),
                value = countryName
            )

            if (!provider.isNullOrBlank()) {
                FundingAccountMetaChip(
                    label = stringResource(R.string.funding_account_hero_provider_label),
                    value = providerLabel(
                        rawProvider = provider,
                        flutterwaveLabel = stringResource(R.string.add_money_provider_flutterwave)
                    )
                )
            }
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
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FundingAccountMetaChip(
    label: String,
    value: String
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

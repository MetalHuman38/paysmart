package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.home.components.dailyLimitsHint
import net.metalbrain.paysmart.ui.home.components.exchangeRateHeadline
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.HomeCardTokens

@Composable
fun AccountInformationCards(
    localSettings: LocalSecuritySettingsModel?,
    countryFlagEmoji: String,
    countryCurrencyCode: String,
    exchangeRateSnapshot: HomeExchangeRateSnapshot,
    onViewRatesClick: () -> Unit,
    onViewAllLimitsClick: () -> Unit
) {
    val context = LocalContext.current
    val baseFlag = CurrencyFlagResolver.resolve(
        context = context,
        currencyCode = exchangeRateSnapshot.baseCurrencyCode
    )
    val targetFlag = CurrencyFlagResolver.resolve(
        context = context,
        currencyCode = exchangeRateSnapshot.targetCurrencyCode,
        preferredCurrencyCode = countryCurrencyCode,
        preferredFlagEmoji = countryFlagEmoji
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.xs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        item {
            AccountInformationCardFrame(
                modifier = Modifier.width(HomeCardTokens.accountInfoCardWidth),
                gradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.14f)
                    )
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = exchangeRateHeadline(exchangeRateSnapshot),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            ExchangeRateFlagChip(
                                baseFlag = baseFlag,
                                targetFlag = targetFlag,
                                modifier = Modifier.padding(start = Dimens.sm)
                            )
                        }
                        Text(
                            text = stringResource(R.string.home_exchange_rate_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        text = stringResource(R.string.home_view_rates),
                        onClick = onViewRatesClick,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            AccountInformationCardFrame(
                modifier = Modifier.width(HomeCardTokens.accountInfoCardWidth),
                gradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
                    )
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        Text(
                            text = stringResource(R.string.home_daily_limits_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dailyLimitsHint(localSettings),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        text = stringResource(R.string.see_all),
                        onClick = onViewAllLimitsClick,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


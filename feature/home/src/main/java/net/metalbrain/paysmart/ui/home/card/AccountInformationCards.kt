package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.home.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.ui.home.components.dailyLimitsHint
import net.metalbrain.paysmart.ui.home.components.exchangeRateHeadline
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.HomeCardTokens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun AccountInformationCards(
    localSettings: LocalSecuritySettingsModel?,
    countryFlagEmoji: String,
    countryCurrencyCode: String,
    exchangeRateSnapshot: HomeExchangeRateSnapshot,
    onViewRatesClick: () -> Unit,
    onViewAllLimitsClick: () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
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
            MetricInformationCard(
                modifier = Modifier.width(HomeCardTokens.accountInfoCardWidth),
                gradient = Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceElevated,
                        colors.info.copy(alpha = 0.12f),
                        colors.brandPrimary.copy(alpha = 0.08f)
                    )
                ),
                kicker = stringResource(R.string.home_exchange_rate_label),
                headline = exchangeRateHeadline(exchangeRateSnapshot),
                supporting = "${exchangeRateSnapshot.baseCurrencyCode} → ${exchangeRateSnapshot.targetCurrencyCode}",
                actionLabel = stringResource(R.string.home_view_rates),
                onClick = onViewRatesClick,
                trailing = {
                    ExchangeRateFlagChip(
                        baseFlag = baseFlag,
                        targetFlag = targetFlag
                    )
                }
            )
        }

        item {
            MetricInformationCard(
                modifier = Modifier.width(HomeCardTokens.accountInfoCardWidth),
                gradient = Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceElevated,
                        colors.brandPrimary.copy(alpha = 0.12f),
                        colors.brandSecondary.copy(alpha = 0.08f)
                    )
                ),
                kicker = stringResource(R.string.home_daily_limits_title),
                headline = stringResource(R.string.home_view_account_limits),
                supporting = dailyLimitsHint(localSettings),
                actionLabel = stringResource(R.string.home_view_account_limits),
                onClick = onViewAllLimitsClick
            )
        }
    }
}

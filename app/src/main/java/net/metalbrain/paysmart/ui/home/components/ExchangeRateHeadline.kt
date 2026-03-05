package net.metalbrain.paysmart.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import java.text.DecimalFormat

@Composable
fun exchangeRateHeadline(snapshot: HomeExchangeRateSnapshot): String {
    if (snapshot.isLoading) return stringResource(R.string.common_processing)
    val rate = snapshot.rate ?: return stringResource(
        R.string.home_exchange_rate_unavailable,
        snapshot.baseCurrencyCode,
        snapshot.targetCurrencyCode
    )
    val formattedRate = DecimalFormat("#,##0.00").format(rate)
    return stringResource(
        R.string.home_exchange_rate_value,
        snapshot.baseCurrencyCode,
        formattedRate,
        snapshot.targetCurrencyCode
    )
}

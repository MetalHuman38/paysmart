package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.home.components.dailyLimitsHint
import net.metalbrain.paysmart.ui.home.components.exchangeRateHeadline
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot

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

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Surface(
                modifier = Modifier.width(310.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exchangeRateHeadline(exchangeRateSnapshot),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$baseFlag$targetFlag",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_exchange_rate_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            Surface(
                modifier = Modifier.width(310.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_daily_limits_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dailyLimitsHint(localSettings),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

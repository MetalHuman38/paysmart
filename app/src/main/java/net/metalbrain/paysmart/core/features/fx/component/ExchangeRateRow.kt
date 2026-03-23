package net.metalbrain.paysmart.core.features.fx.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fx.state.ExchangeRateMarketUiState
import net.metalbrain.paysmart.ui.home.components.ExchangeRateFlagCircle
import net.metalbrain.paysmart.ui.theme.Dimens
import java.text.DecimalFormat

@Composable
fun ExchangeRateRow(
    item: ExchangeRateMarketUiState,
    baseCurrencyCode: String,
    onSendClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.xs,
        shadowElevation = Dimens.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactExchangeRateFlag(flag = flagForBaseCurrency(baseCurrencyCode))
                Text(
                    text = baseCurrencyCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CompactExchangeRateFlag(flag = item.flagEmoji)
                Text(
                    text = item.targetCurrencyCode,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = item.rate?.formatExchangeRate()
                    ?: stringResource(
                        R.string.home_exchange_rate_unavailable,
                        baseCurrencyCode,
                        item.targetCurrencyCode
                    ),
                modifier = Modifier.widthIn(min = 64.dp, max = 96.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            OutlinedButton(
                onClick = onSendClick,
                modifier = Modifier.widthIn(min = 92.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.home_quick_action_send),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CompactExchangeRateFlag(flag: String) {
    ExchangeRateFlagCircle(
        flag = flag,
        modifier = Modifier.widthIn(min = 24.dp),
        textStyle = MaterialTheme.typography.bodySmall
    )
}

private fun Double.formatExchangeRate(): String {
    return DecimalFormat("#,##0.###").format(this)
}

private fun flagForBaseCurrency(baseCurrencyCode: String): String {
    return if (baseCurrencyCode.equals("GBP", ignoreCase = true)) {
        "\uD83C\uDDEC\uD83C\uDDE7"
    } else {
        "\uD83C\uDF10"
    }
}

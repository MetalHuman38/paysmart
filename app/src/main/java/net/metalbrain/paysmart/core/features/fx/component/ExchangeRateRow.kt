package net.metalbrain.paysmart.core.features.fx.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import net.metalbrain.paysmart.ui.home.card.ExchangeRateFlagChip
import net.metalbrain.paysmart.ui.theme.Dimens
import java.text.DecimalFormat

@Composable
fun ExchangeRateRow(
    item: ExchangeRateMarketUiState,
    baseCurrencyCode: String,
    onSendClick: () -> Unit
) {
    BoxWithConstraints {
        val compact = maxWidth < 360.dp
        val baseFlag = flagForBaseCurrency(baseCurrencyCode)
        val pairLabel = "$baseCurrencyCode/${item.targetCurrencyCode}"
        val horizontalPadding = if (compact) 14.dp else Dimens.md
        val verticalPadding = if (compact) 12.dp else 14.dp
        val cardSpacing = if (compact) 10.dp else Dimens.md
        val pairStyle = if (compact) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.titleSmall
        }
        val rateStyle = if (compact) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.titleMedium
        }
        val rateWidth = if (compact) {
            82.dp
        } else {
            96.dp
        }
        val buttonMinWidth = if (compact) {
            84.dp
        } else {
            96.dp
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimens.xs,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) Dimens.sm else Dimens.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExchangeRateFlagChip(
                        baseFlag = baseFlag,
                        targetFlag = item.flagEmoji,
                        minWidth = if (compact) 64.dp else 72.dp,
                        circleMinWidth = if (compact) 24.dp else 28.dp
                    )
                    Text(
                        text = pairLabel,
                        style = pairStyle,
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
                    modifier = Modifier.widthIn(min = 56.dp, max = rateWidth),
                    style = rateStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                OutlinedButton(
                    onClick = onSendClick,
                    modifier = Modifier.widthIn(min = buttonMinWidth),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = sendButtonPadding(compact),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.home_quick_action_send),
                        style = if (compact) {
                            MaterialTheme.typography.labelMedium
                        } else {
                            MaterialTheme.typography.labelLarge
                        }
                    )
                }
            }
        }
    }
}

private fun sendButtonPadding(compact: Boolean): PaddingValues {
    return if (compact) {
        PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    } else {
        PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    }
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

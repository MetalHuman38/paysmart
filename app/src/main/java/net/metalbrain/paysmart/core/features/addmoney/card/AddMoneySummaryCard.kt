package net.metalbrain.paysmart.core.features.addmoney.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiState
import java.util.Locale

@Composable
internal fun AddMoneySummaryCard(
    uiState: AddMoneyUiState,
    currencyFlagEmoji: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.quote?.let { quote ->
                Text(
                    text = stringResource(
                        R.string.add_money_quote_rate_format,
                        quote.sourceCurrency,
                        quote.rate.toString(),
                        quote.targetCurrency
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SummaryLine(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = stringResource(R.string.add_money_summary_paying_in),
                value = "$currencyFlagEmoji ${uiState.currency.uppercase(Locale.US)}"
            )

            SummaryLine(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = stringResource(R.string.add_money_summary_paying_with),
                value = uiState.quoteMethod.label
            )

            SummaryLine(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = stringResource(R.string.add_money_summary_arrives),
                value = uiState.quote?.let {
                    if (it.arrivalSeconds <= 120) {
                        stringResource(R.string.add_money_summary_arrives_today_seconds)
                    } else {
                        stringResource(R.string.add_money_summary_arrives_today)
                    }
                } ?: stringResource(R.string.add_money_summary_arrives_estimate)
            )

            uiState.quote?.let { quote ->
                SummaryLine(
                    icon = {
                        Text(
                            text = "\uD83E\uDDFE",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    label = stringResource(R.string.add_money_summary_you_pay),
                    value = "${quote.sourceAmount} ${quote.sourceCurrency}"
                )

                quote.fees.firstOrNull()?.let { fee ->
                    SummaryLine(
                        icon = {
                            Text(
                                text = "\u2139\uFE0F",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        label = fee.label,
                        value = "${fee.amount} ${quote.sourceCurrency}"
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

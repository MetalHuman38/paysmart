package net.metalbrain.paysmart.core.features.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionFactsCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val factItems = buildList {
        add(
            TransactionFactItem(
                label = stringResource(R.string.transaction_details_amount),
                value = transaction.formattedAmount(includePrefix = true),
                valueColor = transaction.semanticColors().accent
            )
        )
        transaction.provider?.let { provider ->
            add(
                TransactionFactItem(
                    label = stringResource(R.string.transaction_details_provider),
                    value = provider
                )
            )
        }
        transaction.paymentRail?.let { rail ->
            add(
                TransactionFactItem(
                    label = stringResource(R.string.transaction_details_payment_rail),
                    value = rail
                )
            )
        }
        add(
            TransactionFactItem(
                label = stringResource(R.string.transaction_details_status),
                value = transaction.status
            )
        )
        add(
            TransactionFactItem(
                label = stringResource(R.string.transaction_details_date),
                value = transaction.date
            )
        )
        add(
            TransactionFactItem(
                label = stringResource(R.string.transaction_details_time),
                value = transaction.time
            )
        )
        add(
            TransactionFactItem(
                label = stringResource(R.string.transaction_details_reference),
                value = transaction.reference,
                allowMultilineValue = true
            )
        )
        transaction.externalReference?.let { externalReference ->
            add(
                TransactionFactItem(
                    label = stringResource(R.string.transaction_details_external_reference),
                    value = externalReference,
                    allowMultilineValue = true
                )
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.space8),
            verticalArrangement = Arrangement.spacedBy(Dimens.space6)
        ) {
            factItems.forEachIndexed { index, item ->
                TransactionFactLine(
                    label = item.label,
                    value = item.value,
                    valueColor = item.valueColor ?: MaterialTheme.colorScheme.onSurface,
                    allowMultilineValue = item.allowMultilineValue
                )
                if (index < factItems.lastIndex) {
                    TransactionDashedDivider(color = dividerColor)
                }
            }
        }
    }
}

private data class TransactionFactItem(
    val label: String,
    val value: String,
    val valueColor: Color? = null,
    val allowMultilineValue: Boolean = false
)

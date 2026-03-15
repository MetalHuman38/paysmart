package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.transactions.components.formattedAmount
import net.metalbrain.paysmart.core.features.transactions.components.semantic
import net.metalbrain.paysmart.core.features.transactions.components.semanticColors
import net.metalbrain.paysmart.core.features.transactions.components.summaryMetadata
import net.metalbrain.paysmart.core.features.transactions.sheet.TransactionStatusBadge
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionDetailSummary(transaction: Transaction) {
    val colors = transaction.semanticColors()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.space6)
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .background(colors.iconContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(transaction.iconRes),
                contentDescription = null,
                tint = colors.iconTint,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = stringResource(transaction.semantic().summaryLabelRes),
            style = MaterialTheme.typography.titleLarge,
            color = colors.accent,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = transaction.formattedAmount(includePrefix = true),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${transaction.date}, ${transaction.time}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        transaction.summaryMetadata()?.let { metadata ->
            Text(
                text = metadata,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TransactionStatusBadge(status = transaction.status)
    }
}

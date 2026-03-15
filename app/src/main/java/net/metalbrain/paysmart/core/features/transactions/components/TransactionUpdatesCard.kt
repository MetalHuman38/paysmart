package net.metalbrain.paysmart.core.features.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.domain.model.TransactionStatusUpdate
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionUpdatesCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val colors = transaction.semanticColors()
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val timeline = transaction.statusTimeline
        .sortedByDescending { update -> update.timestampMs }
        .ifEmpty {
            listOf(
                TransactionStatusUpdate(
                    status = transaction.status,
                    timestampMs = transaction.updatedAtMs
                )
            )
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
            Text(
                text = stringResource(R.string.transaction_updates_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            timeline.forEachIndexed { index, update ->
                TransactionFactLine(
                    label = update.timestampMs.toTransactionTimelineLabel(),
                    value = update.status,
                    valueColor = if (index == 0) {
                        colors.accent
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (index < timeline.lastIndex) {
                    TransactionDashedDivider(color = dividerColor)
                }
            }
        }
    }
}

private val TRANSACTION_TIMELINE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

private fun Long.toTransactionTimelineLabel(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(TRANSACTION_TIMELINE_FORMAT)
}

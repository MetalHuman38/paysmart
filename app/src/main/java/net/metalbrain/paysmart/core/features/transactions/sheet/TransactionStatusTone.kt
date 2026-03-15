package net.metalbrain.paysmart.core.features.transactions.sheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class TransactionStatusTone(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
internal fun transactionStatusTone(status: String): TransactionStatusTone {
    return when (status.trim().lowercase()) {
        "successful", "completed", "succeeded" -> TransactionStatusTone(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )

        "in progress", "pending", "created" -> TransactionStatusTone(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        "failed", "rejected" -> TransactionStatusTone(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )

        "cancelled", "canceled", "expired" -> TransactionStatusTone(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        else -> TransactionStatusTone(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

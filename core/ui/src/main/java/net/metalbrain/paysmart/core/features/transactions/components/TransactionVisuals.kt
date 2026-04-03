package net.metalbrain.paysmart.core.features.transactions.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.metalbrain.paysmart.core.ui.R
import net.metalbrain.paysmart.domain.model.Transaction
import java.util.Locale

enum class TransactionSemantic(
    @param:StringRes val summaryLabelRes: Int,
    val signedPrefix: String
) {
    AddMoney(R.string.transaction_summary_added, "+"),
    Send(R.string.transaction_summary_sent, "-"),
    Receive(R.string.transaction_summary_received, "+"),
    Activity(R.string.transaction_summary_activity, "")
}

data class TransactionVisualColors(
    val iconContainer: Color,
    val iconTint: Color,
    val accent: Color
)

fun Transaction.semantic(): TransactionSemantic {
    val normalizedTitle = title.trim().lowercase(Locale.US)
    return when {
        normalizedTitle.contains("top up") ||
            normalizedTitle.contains("add money") ||
            iconRes == R.drawable.ic_topup_bank ||
            iconRes == R.drawable.ic_topup_mastercard -> TransactionSemantic.AddMoney

        normalizedTitle.contains("send") ||
            normalizedTitle.contains("paid") ||
            iconRes == R.drawable.ic_send -> TransactionSemantic.Send

        normalizedTitle.contains("receive") ||
            normalizedTitle.contains("received") ||
            normalizedTitle.contains("refund") ||
            iconRes == R.drawable.ic_phone -> TransactionSemantic.Receive

        else -> TransactionSemantic.Activity
    }
}

@Composable
fun Transaction.semanticColors(): TransactionVisualColors {
    return when (semantic()) {
        TransactionSemantic.AddMoney -> TransactionVisualColors(
            iconContainer = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            accent = MaterialTheme.colorScheme.primary
        )

        TransactionSemantic.Send -> TransactionVisualColors(
            iconContainer = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f),
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
            accent = MaterialTheme.colorScheme.tertiary
        )

        TransactionSemantic.Receive -> TransactionVisualColors(
            iconContainer = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            accent = MaterialTheme.colorScheme.secondary
        )

        TransactionSemantic.Activity -> TransactionVisualColors(
            iconContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            accent = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun Transaction.formattedAmount(includePrefix: Boolean = false): String {
    val amountLabel = String.format(Locale.US, "%.2f %s", amount, currency)
    if (!includePrefix) return amountLabel
    val prefix = semantic().signedPrefix
    return if (prefix.isBlank()) amountLabel else prefix + amountLabel
}

fun Transaction.listSubtitle(): String {
    return listOfNotNull(provider, time)
        .joinToString(" • ")
        .ifBlank { time }
}

fun Transaction.summaryMetadata(): String? {
    return listOfNotNull(provider, paymentRail)
        .joinToString(" • ")
        .takeIf { it.isNotBlank() }
}

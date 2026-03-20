package net.metalbrain.paysmart.core.features.help.utils

import net.metalbrain.paysmart.domain.model.Transaction
import java.util.Locale

fun buildSupportRequestBody(
    topicLabel: String,
    transaction: Transaction?,
    message: String
): String {
    return buildString {
        appendLine("Topic: $topicLabel")
        appendLine()

        transaction?.let { selected ->
            appendLine("Transaction")
            appendLine("Title: ${selected.title}")
            appendLine("Amount: ${formatSupportAmount(selected)}")
            appendLine("Status: ${selected.status}")
            appendLine("Date: ${selected.date} ${selected.time}")
            appendLine("Reference: ${selected.reference}")
            selected.externalReference
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value -> appendLine("External reference: $value") }
            selected.provider
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value -> appendLine("Provider: $value") }
            selected.paymentRail
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value -> appendLine("Payment rail: $value") }
            appendLine()
        }

        appendLine("Message")
        appendLine(message)
    }
}

private fun formatSupportAmount(transaction: Transaction): String {
    val amountLabel = String.format(Locale.US, "%.2f %s", transaction.amount, transaction.currency)
    val normalizedTitle = transaction.title.trim().lowercase(Locale.US)
    val prefix = when {
        normalizedTitle.contains("top up") ||
                normalizedTitle.contains("wallet funding") ||
                normalizedTitle.contains("card funding") ||
                normalizedTitle.contains("add money") -> "+"

        normalizedTitle.contains("send") ||
                normalizedTitle.contains("transfer") ||
                normalizedTitle.contains("paid") -> "-"

        else -> ""
    }
    return prefix + amountLabel
}

package net.metalbrain.paysmart.core.features.transactions.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.getSystemService
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.transactions.components.formattedAmount
import net.metalbrain.paysmart.domain.model.Transaction

fun copyTransactionReference(context: Context, reference: String) {
    context.getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText("transaction_reference", reference))
    Toast.makeText(
        context,
        context.getString(R.string.transaction_copy_reference_success),
        Toast.LENGTH_SHORT
    ).show()
}

fun shareTransactionReceipt(context: Context, transaction: Transaction) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, transaction.title)
        putExtra(Intent.EXTRA_TEXT, buildShareText(context, transaction))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.transaction_action_share_receipt)
        )
    )
}

private fun buildShareText(context: Context, transaction: Transaction): String {
    return buildString {
        appendLine(transaction.title)
        appendLine(transaction.formattedAmount())
        transaction.provider?.let { provider ->
            appendLine("${context.getString(R.string.transaction_details_provider)}: $provider")
        }
        appendLine("${context.getString(R.string.transaction_details_status)}: ${transaction.status}")
        appendLine("${context.getString(R.string.transaction_details_date)}: ${transaction.date}")
        appendLine("${context.getString(R.string.transaction_details_time)}: ${transaction.time}")
        transaction.externalReference?.let { externalReference ->
            appendLine(
                "${context.getString(R.string.transaction_details_external_reference)}: $externalReference"
            )
        }
        append("${context.getString(R.string.transaction_details_reference)}: ${transaction.reference}")
    }
}

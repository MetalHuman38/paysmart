package net.metalbrain.paysmart.core.features.fundingaccount.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.getSystemService
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountStatus

fun formatAccountNumber(accountNumber: String): String {
    return accountNumber.trim().chunked(4).joinToString(separator = " ")
}

fun FundingAccountStatus.humanStatus(): String {
    return when (this) {
        FundingAccountStatus.ACTIVE -> "Active"
        FundingAccountStatus.PENDING -> "Pending"
        FundingAccountStatus.DISABLED -> "Disabled"
        FundingAccountStatus.FAILED -> "Failed"
    }
}

fun providerLabel(
    rawProvider: String,
    flutterwaveLabel: String = "Flutterwave"
): String {
    return when (rawProvider.trim().lowercase()) {
        "flutterwave" -> flutterwaveLabel
        else -> rawProvider.ifBlank { flutterwaveLabel }
    }
}

fun copyFundingAccountNumber(context: Context, accountNumber: String) {
    context.getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText("funding_account_number", accountNumber))
    Toast.makeText(
        context,
        context.getString(R.string.funding_account_copy_success),
        Toast.LENGTH_SHORT
    ).show()
}

fun shareFundingAccountDetails(
    context: Context,
    account: FundingAccountData,
    countryName: String,
    countryFlagEmoji: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.funding_account_share_title))
        putExtra(
            Intent.EXTRA_TEXT,
            buildFundingAccountShareText(
                context = context,
                account = account,
                countryName = countryName,
                countryFlagEmoji = countryFlagEmoji
            )
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.funding_account_action_share_details)
        )
    )
}

private fun buildFundingAccountShareText(
    context: Context,
    account: FundingAccountData,
    countryName: String,
    countryFlagEmoji: String
): String {
    return buildString {
        appendLine("$countryFlagEmoji PaySmart $countryName funding account")
        appendLine("${context.getString(R.string.funding_account_details_account_number)}: ${account.accountNumber}")
        appendLine("${context.getString(R.string.funding_account_details_bank_name)}: ${account.bankName}")
        appendLine("${context.getString(R.string.funding_account_details_account_name)}: ${account.accountName}")
        appendLine("${context.getString(R.string.funding_account_details_reference)}: ${account.reference}")
        appendLine(
            "${context.getString(R.string.funding_account_hero_provider_label)}: ${
                providerLabel(
                    rawProvider = account.provider,
                    flutterwaveLabel = context.getString(R.string.add_money_provider_flutterwave)
                )
            }"
        )
        appendLine("${context.getString(R.string.funding_account_details_status)}: ${account.status.humanStatus()}")
        append(context.getString(R.string.funding_account_state_ready_supporting))
    }
}

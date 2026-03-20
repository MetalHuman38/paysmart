package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.OutlinedButton as MaterialOutlinedButton
import net.metalbrain.paysmart.ui.components.OutlinedButton as AppOutlinedButton

@Composable
internal fun BalanceTransferAccountDetailsSection(
    session: AddMoneySessionData,
    isLoading: Boolean,
    onCopyField: (String, String) -> Unit,
    onShareDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val virtualAccount = session.virtualAccount ?: return
    val accountHolderLabel = stringResource(R.string.home_balance_transfer_account_holder_label)
    val bankNameLabel = stringResource(R.string.funding_account_details_bank_name)
    val accountNumberLabel = stringResource(R.string.funding_account_details_account_number)
    val exactAmountLabel = stringResource(R.string.home_balance_transfer_exact_amount_label)
    val referenceLabel = stringResource(R.string.funding_account_details_reference)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        Text(
            text = stringResource(R.string.home_balance_transfer_account_supporting),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HomeDetailSectionCard {
            virtualAccount.accountName?.takeIf { it.isNotBlank() }?.let { accountName ->
                BalanceTransferCopyRow(
                    label = accountHolderLabel,
                    value = accountName,
                    onCopy = { onCopyField(accountHolderLabel, accountName) }
                )

                HorizontalDivider()
            }

            BalanceTransferCopyRow(
                label = bankNameLabel,
                value = virtualAccount.bankName,
                onCopy = { onCopyField(bankNameLabel, virtualAccount.bankName) }
            )

            HorizontalDivider()

            BalanceTransferCopyRow(
                label = accountNumberLabel,
                value = virtualAccount.accountNumber,
                onCopy = { onCopyField(accountNumberLabel, virtualAccount.accountNumber) }
            )

            HorizontalDivider()

            BalanceTransferCopyRow(
                label = exactAmountLabel,
                value = formatMinorCurrencyAmount(session.amountMinor, session.currency),
                onCopy = {
                    onCopyField(
                        exactAmountLabel,
                        formatMinorCurrencyAmount(session.amountMinor, session.currency)
                    )
                }
            )

            HorizontalDivider()

            BalanceTransferCopyRow(
                label = referenceLabel,
                value = virtualAccount.reference,
                onCopy = { onCopyField(referenceLabel, virtualAccount.reference) }
            )

            HorizontalDivider()

            BalanceTransferStaticRow(
                label = stringResource(R.string.home_balance_transfer_expires_label),
                value = session.expiresAtMs.toTransferDateTimeLabel()
            )

            AppOutlinedButton(
                text = stringResource(R.string.funding_account_action_share_details),
                onClick = onShareDetails,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                contentColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                Text(
                    text = stringResource(R.string.home_balance_transfer_instruction_title),
                    style = MaterialTheme.typography.titleMedium
                )

                virtualAccount.note?.takeIf { it.isNotBlank() }?.let { note ->
                    BalanceTransferInstructionItem(text = note)
                }
                BalanceTransferInstructionItem(
                    text = stringResource(R.string.home_balance_transfer_instruction_exact_amount)
                )
                BalanceTransferInstructionItem(
                    text = stringResource(R.string.home_balance_transfer_instruction_one_time)
                )
                BalanceTransferInstructionItem(
                    text = stringResource(
                        R.string.home_balance_transfer_instruction_expiry_format,
                        session.expiresAtMs.toTransferDateTimeLabel()
                    )
                )
                BalanceTransferInstructionItem(
                    text = stringResource(R.string.home_balance_transfer_instruction_confirmation)
                )
            }
        }
    }
}

@Composable
internal fun BalanceTransferAccountLoadingCard(
    modifier: Modifier = Modifier
) {
    HomeDetailSectionCard(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun BalanceTransferCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }

        MaterialOutlinedButton(
            onClick = onCopy,
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = stringResource(R.string.common_copy),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BalanceTransferStaticRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun BalanceTransferInstructionItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMinorCurrencyAmount(amountMinor: Int, currencyCode: String): String {
    val amount = amountMinor.toDouble() / 100.0
    return String.format(Locale.US, "%.2f %s", amount, currencyCode.uppercase(Locale.US))
}

private val TRANSFER_DETAILS_DATE_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

private fun Long.toTransferDateTimeLabel(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(TRANSFER_DETAILS_DATE_TIME_FORMAT)
}

package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.util.formatAccountNumber
import net.metalbrain.paysmart.core.features.fundingaccount.util.humanStatus
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountDetailsCard(
    account: FundingAccountData,
    modifier: Modifier = Modifier
) {
    FundingAccountSurfaceCard(
        modifier = modifier,
        accentColor = MaterialTheme.colorScheme.secondary
    ) {
        Text(
            text = stringResource(R.string.funding_account_details_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = stringResource(R.string.funding_account_details_account_number),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                )
                Text(
                    text = formatAccountNumber(account.accountNumber),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        FundingAccountMetaLine(
            label = stringResource(R.string.funding_account_details_bank_name),
            value = account.bankName
        )
        FundingAccountMetaLine(
            label = stringResource(R.string.funding_account_details_account_name),
            value = account.accountName
        )
        FundingAccountMetaLine(
            label = stringResource(R.string.funding_account_details_reference),
            value = account.reference
        )
        FundingAccountMetaLine(
            label = stringResource(R.string.funding_account_details_status),
            value = account.status.humanStatus()
        )
        account.note?.takeIf { it.isNotBlank() }?.let { note ->
            FundingAccountMetaLine(
                label = stringResource(R.string.funding_account_details_note),
                value = note
            )
        }
    }
}

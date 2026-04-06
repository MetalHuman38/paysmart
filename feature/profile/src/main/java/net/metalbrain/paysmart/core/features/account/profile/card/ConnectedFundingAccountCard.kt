package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.account.profile.components.ConnectedAccountMetaRow
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.util.formatAccountNumber
import net.metalbrain.paysmart.core.features.fundingaccount.util.humanStatus
import net.metalbrain.paysmart.core.features.fundingaccount.util.providerLabel
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ConnectedFundingAccountCard(account: FundingAccountData) {
    val providerText = providerLabel(
        rawProvider = account.provider,
        flutterwaveLabel = stringResource(R.string.add_money_provider_flutterwave)
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = account.bankName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                ConnectedAccountsProviderPill(label = providerText)
                ConnectedAccountsStatusPill(status = account.status.humanStatus())
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
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

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ConnectedAccountMetaRow(
                        label = stringResource(R.string.funding_account_details_bank_name),
                        value = account.bankName
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ConnectedAccountMetaRow(
                        label = stringResource(R.string.funding_account_details_account_name),
                        value = account.accountName
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ConnectedAccountMetaRow(
                        label = stringResource(R.string.funding_account_details_reference),
                        value = account.reference
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ConnectedAccountMetaRow(
                        label = stringResource(R.string.funding_account_details_status),
                        value = account.status.humanStatus()
                    )
                }
            }

            account.note?.takeIf { it.isNotBlank() }?.let { note ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.46f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md),
                        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                    ) {
                        Text(
                            text = stringResource(R.string.funding_account_details_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
                        )
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

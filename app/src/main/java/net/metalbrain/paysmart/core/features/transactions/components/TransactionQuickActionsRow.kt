package net.metalbrain.paysmart.core.features.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionQuickActionsRow(
    onShareReceipt: () -> Unit,
    onCopyReference: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space4)
    ) {
        TransactionQuickActionButton(
            label = stringResource(R.string.transaction_action_share_receipt),
            icon = Icons.Rounded.Share,
            onClick = onShareReceipt,
            modifier = Modifier.weight(1f)
        )
        TransactionQuickActionButton(
            label = stringResource(R.string.transaction_action_copy_reference),
            icon = Icons.Rounded.ContentCopy,
            onClick = onCopyReference,
            modifier = Modifier.weight(1f)
        )
    }
}

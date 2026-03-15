package net.metalbrain.paysmart.core.features.transactions.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.transactions.components.TransactionFactsCard
import net.metalbrain.paysmart.core.features.transactions.components.TransactionQuickActionsRow
import net.metalbrain.paysmart.core.features.transactions.components.TransactionUpdatesCard
import net.metalbrain.paysmart.core.features.transactions.util.copyTransactionReference
import net.metalbrain.paysmart.core.features.transactions.util.shareTransactionReceipt
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionDetailsSheet(transaction: Transaction) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.space10, vertical = Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        TransactionHeroCard(transaction = transaction)
        TransactionQuickActionsRow(
            onShareReceipt = { shareTransactionReceipt(context, transaction) },
            onCopyReference = { copyTransactionReference(context, transaction.reference) }
        )
        TransactionFactsCard(transaction = transaction)
        TransactionUpdatesCard(transaction = transaction)
    }
}

package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.transactions.util.copyTransactionReference
import net.metalbrain.paysmart.core.features.transactions.util.shareTransactionReceipt
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionDetailViewModel

@Composable
fun TransactionDetailRoute(
    transactionId: String,
    viewModel: TransactionDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.load(transactionId)
    }

    TransactionDetailScreen(
        state = state,
        onBack = onBack,
        onShareReceipt = {
            state.transaction?.let { transaction ->
                shareTransactionReceipt(context, transaction)
            }
        },
        onCopyReference = {
            state.transaction?.let { transaction ->
                copyTransactionReference(context, transaction.reference)
            }
        }
    )
}

package net.metalbrain.paysmart.core.features.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionFilterTabs(
    current: TransactionFilter,
    onTabClick: (TransactionFilter) -> Unit
) {
    val tabs = listOf(
        TransactionFilter.All,
        TransactionFilter.Status,
        TransactionFilter.Currency
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.space10),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space4)
    ) {
        items(tabs, key = { it::class.simpleName ?: it.toString() }) { tab ->
            TransactionFilterChip(
                label = tab.label(),
                selected = current::class == tab::class,
                onClick = { onTabClick(tab) }
            )
        }
    }
}

@Composable
private fun TransactionFilter.label(): String = when (this) {
    TransactionFilter.All -> stringResource(R.string.transaction_filter_all)
    TransactionFilter.Status -> stringResource(R.string.transaction_filter_status)
    TransactionFilter.Currency -> stringResource(R.string.transaction_filter_currency)
}

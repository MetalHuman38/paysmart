package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import net.metalbrain.paysmart.ui.theme.Dimens
import kotlin.collections.forEach


@Composable
fun HomeTransactionSearchPanel(
    searchQuery: String,
    availableProviders: List<HomeTransactionProviderFilter>,
    selectedProviders: Set<HomeTransactionProviderFilter>,
    onSearchQueryChange: (String) -> Unit,
    onProviderToggle: (HomeTransactionProviderFilter) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Text(
                text = stringResource(R.string.home_transaction_search_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_clear)
                            )
                        }
                    }
                } else {
                    null
                },
                placeholder = {
                    Text(text = stringResource(R.string.home_transaction_search_placeholder))
                }
            )

            if (availableProviders.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    availableProviders.forEach { provider ->
                        FilterChip(
                            selected = provider in selectedProviders,
                            onClick = { onProviderToggle(provider) },
                            label = {
                                Text(text = provider.displayLabel())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTransactionProviderFilter.displayLabel(): String {
    return when (this) {
        HomeTransactionProviderFilter.STRIPE -> stringResource(R.string.add_money_provider_stripe)
        HomeTransactionProviderFilter.FLUTTERWAVE -> stringResource(R.string.add_money_provider_flutterwave)
    }
}

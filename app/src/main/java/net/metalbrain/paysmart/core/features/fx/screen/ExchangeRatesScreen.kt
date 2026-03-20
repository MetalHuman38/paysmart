package net.metalbrain.paysmart.core.features.fx.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fx.component.ExchangeRateRow
import net.metalbrain.paysmart.core.features.fx.state.ExchangeRatesUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesScreen(
    state: ExchangeRatesUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSendClick: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.exchange_rates_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = stringResource(R.string.send_money_refresh_quote_action)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = Dimens.screenPadding,
                vertical = Dimens.md
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                Text(
                    text = stringResource(R.string.exchange_rates_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                state.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.xl),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.allUnavailable -> {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = Dimens.xs
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimens.lg),
                                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                            ) {
                                Text(
                                    text = stringResource(R.string.exchange_rates_empty_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.exchange_rates_empty_supporting),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    items(
                        items = state.items,
                        key = { item -> item.targetCurrencyCode }
                    ) { item ->
                        ExchangeRateRow(
                            item = item,
                            baseCurrencyCode = state.baseCurrencyCode,
                            onSendClick = { onSendClick(item.targetCurrencyCode) }
                        )
                    }
                }
            }
        }
    }
}

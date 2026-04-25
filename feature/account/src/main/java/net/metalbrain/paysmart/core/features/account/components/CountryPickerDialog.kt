package net.metalbrain.paysmart.core.features.account.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.countryDisplayName
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.utils.rememberDebouncedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerBottomSheet(
    onDismiss: () -> Unit,
    onCountrySelected: (Country) -> Unit,
    selectedCountryIso2: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var rawSearch by remember { mutableStateOf("") }
    val debouncedSearch by rememberDebouncedState(rawSearch, 300L)
    val catalogPriorityIso2 = remember(context) {
        CountrySelectionCatalog.countries(context).map { it.iso2 }.toSet()
    }

    val countries = supportedCountries
        .map { country -> country to countryDisplayName(country) }
        .sortedWith(
            compareBy(
                { if (it.first.isoCode in catalogPriorityIso2) 0 else 1 },
                { it.second.lowercase() }
            )
        )
        .filter { (country, name) ->
            val query = debouncedSearch.trim()
            query.isBlank() ||
                name.contains(query, ignoreCase = true) ||
                country.isoCode.contains(query, ignoreCase = true) ||
                country.dialCode.contains(query, ignoreCase = true)
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.select_country),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = rawSearch,
                onValueChange = { rawSearch = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                items(
                    items = countries,
                    key = { (country, _) -> country.isoCode }
                ) { (country, localizedName) ->
                    val isSelected = country.isoCode.equals(selectedCountryIso2, ignoreCase = true)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) Dimens.xs else 0.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                onCountrySelected(country)
                                onDismiss()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .padding(horizontal = Dimens.md, vertical = Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flagEmoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(Dimens.sm))
                            Text(
                                text = localizedName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = country.dialCode,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(Dimens.sm))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
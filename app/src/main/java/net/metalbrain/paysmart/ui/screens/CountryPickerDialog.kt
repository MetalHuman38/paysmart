package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.utils.rememberDebouncedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerBottomSheet(
    onDismiss: () -> Unit,
    onCountrySelected: (Country) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    var rawSearch by remember { mutableStateOf("") }
    val debouncedSearch by rememberDebouncedState(rawSearch, 300L)

    // Sort and filter countries — done during composition for recomposition on locale change
    val countriesGrouped = supportedCountries
        .map { country -> country to stringResource(country.nameRes) }
        .sortedBy { it.second }
        .filter { (_, name) ->
            name.contains(debouncedSearch, ignoreCase = true)
        }
        .groupBy { (_, name) ->
            name.first().uppercaseChar()
        }.toSortedMap()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 8.dp,
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.select_country),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search field
            OutlinedTextField(
                value = rawSearch,
                onValueChange = { rawSearch = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Country list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                countriesGrouped.forEach { (initial, group) ->
                    stickyHeader {
                        Text(
                            text = initial.toString(),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    items(group) { (country, localizedName) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        onCountrySelected(country)
                                        onDismiss()
                                    }
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(country.flagRes),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = localizedName,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = country.dialCode)
                        }
                    }
                }
            }
        }
    }
}

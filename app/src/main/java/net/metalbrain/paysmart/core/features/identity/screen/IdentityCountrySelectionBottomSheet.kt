package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityCountrySelectionBottomSheet(
    countries: List<IdentityCountryPresentation>,
    selectedCountryIso2: String,
    onDismiss: () -> Unit,
    onCountrySelected: (IdentityCountryPresentation) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    val query = search.trim()
    val filtered = remember(countries, query) {
        if (query.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.name.contains(query, ignoreCase = true) ||
                    country.iso2.contains(query, ignoreCase = true)
            }
        }
    }
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.backgroundPrimary,
        contentColor = colors.textPrimary,
        shape = PaysmartTheme.radius.xLarge
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
                    text = stringResource(R.string.identity_resolver_country_title),
                    style = typography.heading4,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = PaysmartTheme.radius.medium,
                placeholder = { Text(stringResource(R.string.sheet_search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                items(filtered, key = { it.iso2 }) { country ->
                    val isSelected = country.iso2.equals(selectedCountryIso2, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCountrySelected(country)
                                onDismiss()
                            },
                        shape = PaysmartTheme.radius.medium,
                        color = if (isSelected) colors.fillHover else colors.surfaceElevated,
                        contentColor = colors.textPrimary,
                        border = BorderStroke(
                            width = PaysmartTheme.border.thin,
                            color = if (isSelected) {
                                colors.brandPrimary.copy(alpha = 0.36f)
                            } else {
                                colors.borderSubtle
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Dimens.minimumTouchTarget + Dimens.lg)
                                .padding(horizontal = Dimens.md, vertical = Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flag,
                                style = typography.heading4
                            )

                            Spacer(modifier = Modifier.width(Dimens.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.name,
                                    style = typography.bodyMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${country.iso2} - ${country.reviewWindowLabel}",
                                    style = typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.brandPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.components.ClientInformationDatePickerDialog
import net.metalbrain.paysmart.core.features.account.profile.components.StatementFieldContainer
import net.metalbrain.paysmart.core.features.account.profile.components.StatementFieldSection
import net.metalbrain.paysmart.core.features.account.profile.data.repository.AccountStatementCurrencyOption
import net.metalbrain.paysmart.core.features.account.profile.data.repository.AccountStatementDatePickerTarget
import net.metalbrain.paysmart.core.features.account.profile.state.AccountStatementFormat
import net.metalbrain.paysmart.core.features.account.profile.state.AccountStatementUiState
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.language.component.CompactLanguageFlag
import net.metalbrain.paysmart.ui.components.CatalogSelectionBottomSheet
import net.metalbrain.paysmart.ui.components.CatalogSelectionOption
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountStatementScreen(
    state: AccountStatementUiState,
    onBack: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onFormatSelected: (AccountStatementFormat) -> Unit
) {
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val earliestStatementDate = remember(today) { today.minusYears(5) }
    val exportPendingMessage = stringResource(R.string.account_statement_export_pending)
    var showCurrencySheet by rememberSaveable { mutableStateOf(false) }
    var pickerTarget by rememberSaveable { mutableStateOf(AccountStatementDatePickerTarget.NONE) }

    val currencyOptions = remember(resources) {
        CountrySelectionCatalog.currencies(resources).map { currency ->
            AccountStatementCurrencyOption(
                code = currency.code,
                displayName = currency.displayName,
                flagEmoji = currency.flagEmoji
            )
        }
    }
    val selectedCurrencyOption = currencyOptions.firstOrNull { option ->
        option.code.equals(state.selectedCurrencyCode, ignoreCase = true)
    } ?: currencyOptions.firstOrNull() ?: AccountStatementCurrencyOption(
        code = state.selectedCurrencyCode.ifBlank { "GBP" },
        displayName = state.selectedCurrencyCode.ifBlank { "GBP" },
        flagEmoji = "\uD83C\uDF10"
    )
    val currencySheetOptions = remember(currencyOptions) {
        currencyOptions.map { option ->
            CatalogSelectionOption(
                key = option.code,
                title = option.code,
                subtitle = option.displayName,
                leadingEmoji = option.flagEmoji
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.account_statement_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = Dimens.xs
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.account_statement_get_action),
                        enabled = state.canRequestStatement,
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = exportPendingMessage
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.lg)
            ) {
                Text(
                    text = stringResource(R.string.account_statement_supporting),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.lg)
                ) {
                    StatementFieldSection(
                        title = stringResource(R.string.account_statement_currency_label)
                    ) {
                        StatementFieldContainer(
                            onClick = { showCurrencySheet = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompactLanguageFlag(flagEmoji = selectedCurrencyOption.flagEmoji)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                                ) {
                                    Text(
                                        text = selectedCurrencyOption.code,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = selectedCurrencyOption.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    StatementFieldSection(
                        title = stringResource(R.string.account_statement_start_date_label)
                    ) {
                        StatementFieldContainer(
                            onClick = { pickerTarget = AccountStatementDatePickerTarget.START }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.startDate?.format(STATEMENT_DATE_FORMATTER)
                                        ?: stringResource(R.string.account_statement_date_placeholder),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (state.startDate == null) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    StatementFieldSection(
                        title = stringResource(R.string.account_statement_end_date_label)
                    ) {
                        StatementFieldContainer(
                            onClick = { pickerTarget = AccountStatementDatePickerTarget.END }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.endDate?.format(STATEMENT_DATE_FORMATTER)
                                        ?: stringResource(R.string.account_statement_date_placeholder),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (state.endDate == null) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    StatementFieldSection(
                        title = stringResource(R.string.account_statement_format_label)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = Dimens.xs,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        role = Role.RadioButton,
                                        onClick = { onFormatSelected(AccountStatementFormat.PDF) }
                                    )
                                    .padding(horizontal = Dimens.sm, vertical = Dimens.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                            ) {
                                RadioButton(
                                    selected = state.format == AccountStatementFormat.PDF,
                                    onClick = { onFormatSelected(AccountStatementFormat.PDF) }
                                )
                                Text(
                                    text = stringResource(R.string.account_statement_format_pdf),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (pickerTarget) {
        AccountStatementDatePickerTarget.START -> {
            ClientInformationDatePickerDialog(
                initialDate = state.startDate ?: state.endDate ?: today,
                earliestDate = earliestStatementDate,
                latestDate = state.endDate ?: today,
                onDismiss = { pickerTarget = AccountStatementDatePickerTarget.NONE },
                onDateSelected = { selectedDate ->
                    pickerTarget = AccountStatementDatePickerTarget.NONE
                    onStartDateSelected(selectedDate)
                }
            )
        }

        AccountStatementDatePickerTarget.END -> {
            ClientInformationDatePickerDialog(
                initialDate = state.endDate ?: state.startDate ?: today,
                earliestDate = state.startDate ?: earliestStatementDate,
                latestDate = today,
                onDismiss = { pickerTarget = AccountStatementDatePickerTarget.NONE },
                onDateSelected = { selectedDate ->
                    pickerTarget = AccountStatementDatePickerTarget.NONE
                    onEndDateSelected(selectedDate)
                }
            )
        }

        AccountStatementDatePickerTarget.NONE -> Unit
    }

    if (showCurrencySheet) {
        CatalogSelectionBottomSheet(
            title = stringResource(R.string.account_statement_currency_label),
            options = currencySheetOptions,
            selectedKey = selectedCurrencyOption.code,
            onDismiss = { showCurrencySheet = false },
            onSelect = { selected ->
                onCurrencySelected(selected.key)
            }
        )
    }
}


private val STATEMENT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.getDefault())

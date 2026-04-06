package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.creation.components.AccountCreationScaffold
import net.metalbrain.paysmart.core.features.account.creation.components.ClientInformationContent
import net.metalbrain.paysmart.ui.components.ClientInformationDatePickerDialog
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun ClientInformationScreen(
    countryIso2: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: ClientInformationViewModel = hiltViewModel()
) {
    LaunchedEffect(countryIso2) {
        viewModel.bindCountry(countryIso2)
    }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val market = CountrySelectionCatalog.countryByIso2(context, countryIso2)
    var showDatePicker by remember { mutableStateOf(false) }

    val latestDob = LocalDate.now().minusYears(18)
    val earliestDob = LocalDate.now().minusYears(120)
    val initialDob = rememberDateOrDefault(state.dateOfBirth)
        .coerceInDateRange(earliestDob, latestDob)

    AccountCreationScaffold(onBack = onBack) { innerPadding ->
        ClientInformationContent(
            state = state,
            heroEmoji = market?.flagEmoji
                ?: CountrySelectionCatalog.flagForCountry(context, countryIso2),
            heroSubtitle = market?.name ?: countryIso2,
            onFirstNameChanged = viewModel::onFirstNameChanged,
            onMiddleNameChanged = viewModel::onMiddleNameChanged,
            onLastNameChanged = viewModel::onLastNameChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onShowDatePicker = { showDatePicker = true },
            onContinue = {
                viewModel.clearError()
                viewModel.submit(onSuccess = onContinue)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.space6)
        )
    }

    if (showDatePicker) {
        ClientInformationDatePickerDialog(
            title = stringResource(R.string.client_info_pick_date_of_birth),
            initialDate = initialDob,
            earliestDate = earliestDob,
            latestDate = latestDob,
            onDismiss = { showDatePicker = false },
            onDateSelected = { picked ->
                viewModel.onDateOfBirthChanged(picked.toString())
                showDatePicker = false
            }
        )
    }
}

private fun rememberDateOrDefault(raw: String): LocalDate {
    return try {
        LocalDate.parse(raw.trim())
    } catch (_: DateTimeParseException) {
        LocalDate.now().minusYears(30)
    }
}

private fun LocalDate.coerceInDateRange(min: LocalDate, max: LocalDate): LocalDate {
    return when {
        isBefore(min) -> min
        isAfter(max) -> max
        else -> this
    }
}

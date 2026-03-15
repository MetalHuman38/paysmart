package net.metalbrain.paysmart.core.features.account.creation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.components.AccountCreationScaffold
import net.metalbrain.paysmart.core.features.account.creation.components.ClientInformationContent
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.LocalDate
import java.time.ZoneId
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

    val initialDob = rememberDateOrDefault(state.dateOfBirth)

    fun showDatePicker() {
        val zone = ZoneId.systemDefault()
        val latestDob = LocalDate.now().minusYears(18)
        val earliestDob = LocalDate.now().minusYears(120)
        val dialog = DatePickerDialog(
            context,
            R.style.SpinnerDatePickerDialog,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.onDateOfBirthChanged(picked.toString())
            },
            initialDob.year,
            initialDob.monthValue - 1,
            initialDob.dayOfMonth
        )
        dialog.datePicker.maxDate = latestDob
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        dialog.datePicker.minDate = earliestDob
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        dialog.show()
    }

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
            onShowDatePicker = ::showDatePicker,
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
}

private fun rememberDateOrDefault(raw: String): LocalDate {
    return try {
        LocalDate.parse(raw.trim())
    } catch (_: DateTimeParseException) {
        LocalDate.now().minusYears(30)
    }
}

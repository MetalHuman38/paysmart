package net.metalbrain.paysmart.core.features.account.creation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.client_info_legal_name_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.firstName,
                    onValueChange = viewModel::onFirstNameChanged,
                    label = { Text(stringResource(R.string.client_info_first_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.middleName,
                    onValueChange = viewModel::onMiddleNameChanged,
                    label = { Text(stringResource(R.string.client_info_middle_name_optional)) },
                    singleLine = true
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.lastName,
                onValueChange = viewModel::onLastNameChanged,
                label = { Text(stringResource(R.string.client_info_last_name)) },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text(stringResource(R.string.client_info_email_address)) },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dateOfBirth,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.client_info_date_of_birth)) },
                placeholder = { Text(stringResource(R.string.client_info_date_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = ::showDatePicker) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = stringResource(R.string.client_info_pick_date_of_birth)
                        )
                    }
                },
                enabled = true
            )

            Text(
                text = stringResource(R.string.client_info_age_requirement),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = stringResource(R.string.continue_text),
                onClick = {
                    viewModel.clearError()
                    viewModel.submit(onSuccess = onContinue)
                },
                enabled = state.canContinue && !state.isSaving,
                isLoading = state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }
    }
}

private fun rememberDateOrDefault(raw: String): LocalDate {
    return try {
        LocalDate.parse(raw.trim())
    } catch (_: DateTimeParseException) {
        LocalDate.now().minusYears(30)
    }
}

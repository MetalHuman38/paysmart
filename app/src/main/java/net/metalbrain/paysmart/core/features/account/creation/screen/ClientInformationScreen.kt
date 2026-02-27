package net.metalbrain.paysmart.core.features.account.creation.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    text = "Enter your full legal name exactly as it appears on your government ID.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.firstName,
                    onValueChange = viewModel::onFirstNameChanged,
                    label = { Text("First name") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.middleName,
                    onValueChange = viewModel::onMiddleNameChanged,
                    label = { Text("Middle name (optional)") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.lastName,
                onValueChange = viewModel::onLastNameChanged,
                label = { Text("Last name") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email address") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dateOfBirth,
                onValueChange = viewModel::onDateOfBirthChanged,
                label = { Text("Date of birth (YYYY-MM-DD)") },
                singleLine = true
            )

            Text(
                text = "You must be 18 or older to use PaySmart.",
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
                text = "Continue",
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

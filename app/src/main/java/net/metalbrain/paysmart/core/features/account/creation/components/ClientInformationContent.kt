package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.card.AccountCreationHeroCard
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun ClientInformationContent(
    state: ClientInformationUiState,
    heroEmoji: String,
    heroSubtitle: String,
    onFirstNameChanged: (String) -> Unit,
    onMiddleNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onShowDatePicker: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        AccountCreationHeroCard(
            emoji = heroEmoji,
            title = stringResource(R.string.post_otp_security_step_legal_title),
            subtitle = heroSubtitle
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.client_info_legal_name_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Dimens.space10)
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimens.space10),
                verticalArrangement = Arrangement.spacedBy(Dimens.space6)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.firstName,
                    onValueChange = onFirstNameChanged,
                    label = { Text(stringResource(R.string.client_info_first_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.middleName,
                    onValueChange = onMiddleNameChanged,
                    label = { Text(stringResource(R.string.client_info_middle_name_optional)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.lastName,
                    onValueChange = onLastNameChanged,
                    label = { Text(stringResource(R.string.client_info_last_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.email,
                    onValueChange = onEmailChanged,
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
                        IconButton(onClick = onShowDatePicker) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = stringResource(
                                    R.string.client_info_pick_date_of_birth
                                )
                            )
                        }
                    }
                )
            }
        }

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
            onClick = onContinue,
            enabled = state.canContinue && !state.isSaving,
            isLoading = state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight)
        )
    }
}

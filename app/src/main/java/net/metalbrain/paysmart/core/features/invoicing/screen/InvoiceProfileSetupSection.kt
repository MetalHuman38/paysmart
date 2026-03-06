package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState

@Composable
fun InvoiceProfileSetupSection(
    state: InvoiceSetupUiState,
    onFullNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onBadgeNumberChanged: (String) -> Unit,
    onBadgeExpiryChanged: (String) -> Unit,
    onUtrChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onSortCodeChanged: (String) -> Unit,
    onPaymentInstructionsChanged: (String) -> Unit,
    onDefaultRateChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = stringResource(R.string.invoice_setup_profile_title))
            Text(text = stringResource(R.string.invoice_setup_profile_hint))
            OutlinedTextField(
                value = state.profileDraft.fullName,
                onValueChange = onFullNameChanged,
                label = { Text(stringResource(R.string.invoice_setup_full_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.address,
                onValueChange = onAddressChanged,
                label = { Text(stringResource(R.string.invoice_setup_address_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.profileDraft.badgeNumber,
                onValueChange = onBadgeNumberChanged,
                label = { Text(stringResource(R.string.invoice_setup_badge_number_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            InvoiceDateField(
                value = state.profileDraft.badgeExpiryDate,
                label = stringResource(R.string.invoice_setup_badge_expiry_label),
                onDateSelected = onBadgeExpiryChanged,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.profileDraft.utrNumber,
                onValueChange = onUtrChanged,
                label = { Text(stringResource(R.string.invoice_setup_utr_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.email,
                onValueChange = onEmailChanged,
                label = { Text(stringResource(R.string.invoice_setup_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.contactPhone,
                onValueChange = onPhoneChanged,
                label = { Text(stringResource(R.string.invoice_setup_phone_optional_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.accountNumber,
                onValueChange = onAccountNumberChanged,
                label = { Text(stringResource(R.string.invoice_setup_account_number_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.sortCode,
                onValueChange = onSortCodeChanged,
                label = { Text(stringResource(R.string.invoice_setup_sort_code_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.profileDraft.paymentInstructions,
                onValueChange = onPaymentInstructionsChanged,
                label = { Text(stringResource(R.string.invoice_setup_payment_instructions_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.profileDraft.defaultHourlyRateInput,
                onValueChange = onDefaultRateChanged,
                label = { Text(stringResource(R.string.invoice_setup_default_rate_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.ui.theme.Dimens

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
    InvoiceSurfaceCard {
        InvoiceSectionHeading(
            title = stringResource(R.string.invoice_setup_profile_title),
            body = stringResource(R.string.invoice_setup_profile_hint)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            InvoiceInputField(
                value = state.profileDraft.fullName,
                onValueChange = onFullNameChanged,
                label = stringResource(R.string.invoice_setup_full_name_label)
            )
            InvoiceInputField(
                value = state.profileDraft.address,
                onValueChange = onAddressChanged,
                label = stringResource(R.string.invoice_setup_address_label),
                singleLine = false
            )
            InvoiceInputField(
                value = state.profileDraft.badgeNumber,
                onValueChange = onBadgeNumberChanged,
                label = stringResource(R.string.invoice_setup_badge_number_label)
            )
            InvoiceDateField(
                value = state.profileDraft.badgeExpiryDate,
                label = stringResource(R.string.invoice_setup_badge_expiry_label),
                onDateSelected = onBadgeExpiryChanged
            )
            InvoiceInputField(
                value = state.profileDraft.utrNumber,
                onValueChange = onUtrChanged,
                label = stringResource(R.string.invoice_setup_utr_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            InvoiceInputField(
                value = state.profileDraft.email,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.invoice_setup_email_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            InvoiceInputField(
                value = state.profileDraft.contactPhone,
                onValueChange = onPhoneChanged,
                label = stringResource(R.string.invoice_setup_phone_optional_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            InvoiceInputField(
                value = state.profileDraft.accountNumber,
                onValueChange = onAccountNumberChanged,
                label = stringResource(R.string.invoice_setup_account_number_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            InvoiceInputField(
                value = state.profileDraft.sortCode,
                onValueChange = onSortCodeChanged,
                label = stringResource(R.string.invoice_setup_sort_code_label)
            )
            InvoiceInputField(
                value = state.profileDraft.paymentInstructions,
                onValueChange = onPaymentInstructionsChanged,
                label = stringResource(R.string.invoice_setup_payment_instructions_label),
                singleLine = false
            )
            InvoiceInputField(
                value = state.profileDraft.defaultHourlyRateInput,
                onValueChange = onDefaultRateChanged,
                label = stringResource(R.string.invoice_setup_default_rate_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

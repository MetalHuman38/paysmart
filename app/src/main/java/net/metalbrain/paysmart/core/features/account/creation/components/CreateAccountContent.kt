package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.card.AccountCreationHeroCard
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.ui.components.AccountSwitchPrompt
import net.metalbrain.paysmart.ui.components.AccountSwitchVariant
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.TermsAndPrivacyText
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

/**
 * A Composable that represents the content of the account creation screen.
 *
 * This component displays the UI for entering a phone number, selecting a country,
 * accepting marketing and legal terms, and provides the actions to continue or
 * switch to the sign-in flow.
 *
 * @param selectedCountry The currently selected country for the phone number prefix.
 * @param phoneNumber The current value of the phone number input field.
 * @param acceptedMarketing Whether the user has accepted the marketing consent.
 * @param acceptedTerms Whether the user has accepted the terms and conditions.
 * @param isSubmitting Indicates if the account creation process is currently in progress.
 * @param errorMessage An optional error message to display if the registration fails.
 * @param isContinueEnabled Whether the continue button should be interactive.
 * @param onFlagClick Callback triggered when the country/flag selection is clicked.
 * @param onPhoneNumberChange Callback triggered when the phone number input changes.
 * @param onToggleMarketing Callback triggered to toggle the marketing consent checkbox.
 * @param onToggleTerms Callback triggered to toggle the terms and conditions checkbox.
 * @param onContinue Callback triggered when the user clicks the continue button.
 * @param onSignInClicked Callback triggered when the user clicks the link to sign in instead.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
internal fun CreateAccountContent(
    selectedCountry: Country,
    phoneNumber: String,
    acceptedMarketing: Boolean,
    acceptedTerms: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    isContinueEnabled: Boolean,
    onFlagClick: () -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onToggleMarketing: () -> Unit,
    onToggleTerms: () -> Unit,
    onContinue: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PaysmartTheme.colorTokens

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        AccountCreationHeroCard(
            title = stringResource(R.string.lets_get_started),
            subtitle = stringResource(R.string.enter_phone_to_signup)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfacePrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {

                CountryHeaderRow(
                    country = selectedCountry,
                    onClick = onFlagClick
                )

                PhoneNumberInput(
                    selectedCountry = selectedCountry,
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = onPhoneNumberChange,
                    onFlagClick = onFlagClick
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfacePrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                ConsentRow(
                    checked = acceptedMarketing,
                    onCheckedChange = { onToggleMarketing() }
                ) {
                    Text(
                        text = stringResource(R.string.marketing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }

                ConsentRow(
                    checked = acceptedTerms,
                    onCheckedChange = { onToggleTerms() }
                ) {
                    TermsAndPrivacyText(
                        onTermsClicked = {},
                        onPrivacyClicked = {}
                    )
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.continue_text),
            onClick = onContinue,
            enabled = isContinueEnabled,
            isLoading = isSubmitting,
            loadingText = stringResource(R.string.common_processing)
        )

        if (!errorMessage.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = colors.error.copy(alpha = 0.12f)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error
                )
            }
        }

        AccountSwitchPrompt(
            variant = AccountSwitchVariant.HAVE_ACCOUNT,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onActionClick = onSignInClicked,
        )
    }
}

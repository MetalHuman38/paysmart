package net.metalbrain.paysmart.core.features.account.creation.screen

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.ui.components.AccountSwitchPrompt
import net.metalbrain.paysmart.ui.components.AccountSwitchVariant
import net.metalbrain.paysmart.ui.components.PhoneAlreadyRegisteredSheet
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SmallTextButton
import net.metalbrain.paysmart.ui.components.TermsAndPrivacyText
import net.metalbrain.paysmart.ui.screens.CountryPickerBottomSheet
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.utils.detectDeviceCountryIso2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onVerificationContinue: (String) -> Unit,
    onGetHelpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onBackClicked: () -> Unit
) {

    var showCountryPicker by remember { mutableStateOf(false) }
    val selectedCountry by viewModel.selectedCountry
    var showAlreadyRegisteredSheet by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.autoSelectCountry(detectDeviceCountryIso2(context))
    }

    if (showAlreadyRegisteredSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlreadyRegisteredSheet = false },
            shape = MaterialTheme.shapes.large
        ) {
            PhoneAlreadyRegisteredSheet(
                onDismiss = { showAlreadyRegisteredSheet = false }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.mediumSpacing)
            .padding(bottom = Dimens.mediumSpacing)
            .padding(horizontal = Dimens.screenPadding)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.smallSpacing))
            Spacer(modifier = Modifier.weight(1f))
            SmallTextButton(
                text = stringResource(R.string.get_help),
                onClick = onGetHelpClicked,
            )
        }

        Spacer(Modifier.height(Dimens.mediumSpacing))

        Text(
            text = stringResource(R.string.lets_get_started),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Dimens.mediumSpacing))

        Surface(
            shape = RoundedCornerShape(Dimens.cornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = Dimens.smallSpacing
        ) {
            Text(
                text = stringResource(R.string.enter_phone_to_signup),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Dimens.mediumSpacing)
            )
        }

        Spacer(Modifier.height(Dimens.mediumSpacing))

        PhoneNumberInput(
            selectedCountry = selectedCountry,
            phoneNumber = viewModel.phoneNumber,
            onPhoneNumberChange = viewModel::onPhoneNumberChanged,
            onFlagClick = { showCountryPicker = true }
        )

        Spacer(Modifier.height(Dimens.mediumSpacing))

        Surface(
            shape = RoundedCornerShape(Dimens.cornerRadius),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimens.smallSpacing
        ) {
            Column(
                modifier = Modifier.padding(Dimens.mediumSpacing)
            ) {
                ConsentRow(
                    checked = viewModel.acceptedMarketing,
                    onCheckedChange = { viewModel.onToggleMarketing() }
                ) {
                    Text(
                        text = stringResource(R.string.marketing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

                ConsentRow(
                    checked = viewModel.acceptedTerms,
                    onCheckedChange = { viewModel.onToggleTerms() }
                ) {
                    TermsAndPrivacyText(
                        onTermsClicked = {},
                        onPrivacyClicked = {}
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        PrimaryButton(
            text = stringResource(R.string.continue_text),
            onClick = {
                isSubmitting = true
                scope.launch {
                    viewModel.startPhoneVerification(
                        activity = activity,
                        onSuccess = {
                            isSubmitting = false
                            onVerificationContinue(normalizeCountryIso2(selectedCountry.isoCode))
                        },
                        onPhoneAlreadyRegistered = {
                            isSubmitting = false
                            showAlreadyRegisteredSheet = true
                        },
                        onError = { error ->
                            isSubmitting = false
                            Log.e("PhoneAuth", error.message ?: "Unknown error")
                        }
                    )
                }
            },
            enabled = viewModel.acceptedTerms && viewModel.isPhoneValid(),
            isLoading = isSubmitting,
            loadingText = stringResource(R.string.common_processing)
        )

        Spacer(modifier = Modifier.height(Dimens.smallSpacing))

        AccountSwitchPrompt(
            variant = AccountSwitchVariant.HAVE_ACCOUNT,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = Dimens.smallSpacing),
            onActionClick = onSignInClicked,
        )
    }

    if (showCountryPicker) {
        CountryPickerBottomSheet(
            onDismiss = { showCountryPicker = false },
            onCountrySelected = { selected ->
                viewModel.onCountrySelected(selected)
                showCountryPicker = false
            }
        )
    }

}

@Composable
private fun ConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.width(Dimens.smallSpacing))
        Column(
            modifier = Modifier.padding(top = Dimens.smallSpacing)
        ) {
            content()
        }
    }
}

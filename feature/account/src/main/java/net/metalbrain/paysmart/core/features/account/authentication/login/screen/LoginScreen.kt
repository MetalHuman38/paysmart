package net.metalbrain.paysmart.core.features.account.authentication.login.screen

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthIntent
import net.metalbrain.paysmart.core.features.account.authentication.login.card.LoginSectionCard
import net.metalbrain.paysmart.core.features.account.authentication.login.utils.LoginDivider
import net.metalbrain.paysmart.core.features.account.authentication.login.utils.LoginHeaderRow
import net.metalbrain.paysmart.core.features.account.authentication.login.utils.LoginRecoveryRow
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.components.CountryPickerBottomSheet
import net.metalbrain.paysmart.core.features.account.components.GoogleSignInBtn
import net.metalbrain.paysmart.core.features.account.components.PhoneNumberInput
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.domain.model.detectDeviceCountryIso2
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.AccountSwitchPrompt
import net.metalbrain.paysmart.ui.components.AccountSwitchVariant
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.BackendErrorModal
import net.metalbrain.paysmart.ui.components.EmailSignInBtn
import net.metalbrain.paysmart.ui.components.FacebookSignInButton
import net.metalbrain.paysmart.ui.components.PasskeySignBtn
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    clientId: String,
    currentLanguage: String,
    viewModel: LoginViewModel,
    reauthOtpViewModel: ReauthOtpViewModel,
    onContinue: () -> Unit,
    onMfaRequired: () -> Unit,
    onBackClicked: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToReauth: () -> Unit,
    onNavigateToEmailSent: (email: String) -> Unit,
) {
    val selectedCountry by viewModel.selectedCountry
    val phoneNumber = viewModel.phoneNumber
    val isAuthLoading = viewModel.loading
    val isPasskeyLoading = viewModel.passkeyLoading
    var showCountryPicker by remember { mutableStateOf(false) }
    val backendError = remember { mutableStateOf<String?>(null) }
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(activity?.intent?.dataString) {
        val currentIntent = activity?.intent ?: return@LaunchedEffect
        viewModel.handleEmailLoginFromIntent(
            intent = currentIntent,
            onSuccess = onContinue,
            onMfaRequired = onMfaRequired,
            onError = { error ->
                backendError.value = parseBackendError(error)
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.autoSelectCountry(detectDeviceCountryIso2(context))
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Google credential retrieval is handled by the provider callback.
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            LoginHeaderRow(
                currentLanguage = currentLanguage,
                onBackClicked = onBackClicked,
                onLanguageClick = onNavigateToLanguage,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                AuthScreenTitle(text = stringResource(R.string.welcome_back))
                AuthScreenSubtitle(text = stringResource(R.string.enter_phone_to_associated_to_account))
            }

            LoginSectionCard {
                Column(
                    modifier = Modifier.padding(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    PhoneNumberInput(
                        selectedCountry = selectedCountry,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = viewModel::onPhoneNumberChanged,
                        onFlagClick = { showCountryPicker = true }
                    )

                    PrimaryButton(
                        onClick = {
                            if (activity != null) {
                                if (viewModel.phoneNumber.isNotBlank()) {
                                    reauthOtpViewModel.startReauthFlow(activity)
                                    onNavigateToReauth()
                                } else {
                                    reauthOtpViewModel.errorHandled()
                                }
                            } else {
                                reauthOtpViewModel.errorHandled()
                            }
                        },
                        text = stringResource(R.string.continue_text),
                        enabled = phoneNumber.isNotBlank(),
                        isLoading = isAuthLoading,
                        loadingText = stringResource(R.string.common_processing)
                    )

                    LoginRecoveryRow(onForgotPassword = onForgotPassword)
                }
            }

            backendError.value?.let { message ->
                BackendErrorModal(
                    message = message,
                    onDismiss = { backendError.value = null }
                )
            }

            LoginSectionCard {
                Column(
                    modifier = Modifier.padding(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    LoginDivider()

                    EmailSignInBtn(
                        email = viewModel.email.value,
                        onLinkSent = {
                            viewModel.sendMagicLink(
                                context = context,
                                email = viewModel.email.value,
                                onSuccess = {
                                    onNavigateToEmailSent(viewModel.email.value)
                                },
                                onError = { error ->
                                    backendError.value = parseBackendError(error)
                                }
                            )
                        },
                        onError = { error ->
                            backendError.value = parseBackendError(error)
                        },
                        enabled = !isAuthLoading,
                        isLoading = isAuthLoading
                    )

                    GoogleSignInBtn(
                        clientId = clientId,
                        launcher = googleLauncher,
                        intent = GoogleAuthIntent.SIGN_IN,
                        enabled = !isAuthLoading,
                        isLoading = isAuthLoading,
                        loadingText = stringResource(R.string.common_processing),
                        onCredentialReceived = {
                            viewModel.handleGoogleSignIn(
                                it,
                                GoogleAuthIntent.SIGN_IN,
                                onSuccess = onContinue,
                                onMfaRequired = onMfaRequired
                            ) { error ->
                                backendError.value = parseBackendError(error)
                            }
                        },
                        onError = {
                            backendError.value = parseBackendError(it)
                        }
                    )

                    activity?.let { currentActivity ->
                        FacebookSignInButton(
                            activity = currentActivity,
                            enabled = !isAuthLoading,
                            isLoading = isAuthLoading,
                            loadingText = stringResource(R.string.common_processing),
                            onClick = {
                                viewModel.handleFacebookLogin(
                                    activity = currentActivity,
                                    onSuccess = onContinue,
                                    onMfaRequired = onMfaRequired,
                                    onError = { error ->
                                        backendError.value = parseBackendError(error)
                                    }
                                )
                            }
                        )
                    }

                    PasskeySignBtn(
                        enabled = !isAuthLoading && !isPasskeyLoading,
                        isLoading = isPasskeyLoading,
                        loadingText = stringResource(R.string.common_processing),
                        onClick = {
                            activity?.let { currentActivity ->
                                viewModel.signInWithPasskey(
                                    activity = currentActivity,
                                    autoAttempt = false,
                                    onSuccess = onContinue,
                                    onError = { error ->
                                        backendError.value = parseBackendError(error)
                                    }
                                )
                            }
                        }
                    )
                }
            }

            AccountSwitchPrompt(
                variant = AccountSwitchVariant.DONT_HAVE_ACCOUNT,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.xs),
                onActionClick = onSignUp
            )
        }

        if (showCountryPicker) {
            CountryPickerBottomSheet(
                onDismiss = { showCountryPicker = false },
                onCountrySelected = {
                    viewModel.onCountrySelected(it)
                    showCountryPicker = false
                },
                selectedCountryIso2 = selectedCountry.isoCode
            )
        }
    }
}

private fun parseBackendError(e: Throwable): String {
    val msg = e.message ?: return "Something went wrong"
    return msg.substringAfter("BLOCKING_FUNCTION_ERROR_RESPONSE:", missingDelimiterValue = msg)
        .replace("}", "")
        .replace("{", "")
        .trim()
}

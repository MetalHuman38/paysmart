package net.metalbrain.paysmart.core.features.account.authentication.login.screen


import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.components.AccountSwitchPrompt
import net.metalbrain.paysmart.ui.components.AccountSwitchVariant
import net.metalbrain.paysmart.ui.components.BackendErrorModal
import net.metalbrain.paysmart.ui.components.EmailSignInBtn
import net.metalbrain.paysmart.ui.components.FacebookSignInButton
import net.metalbrain.paysmart.ui.components.GoogleSignInBtn
import net.metalbrain.paysmart.ui.components.LanguageSelector
import net.metalbrain.paysmart.ui.components.PhoneNumberInput
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.GoogleAuthIntent
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.screens.CountryPickerBottomSheet
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.utils.extractSimpleBackendError


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel,
    reauthOtpViewModel: ReauthOtpViewModel,
    languageViewModel: LanguageViewModel,
    onContinue: () -> Unit,
    onBackClicked: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit
) {
    val selectedCountry by viewModel.selectedCountry
    val phoneNumber = viewModel.phoneNumber
    val isAuthLoading = viewModel.loading
    val currentLang by languageViewModel.currentLanguage.collectAsState()
    var showCountryPicker by remember { mutableStateOf(false) }
    val backendError = remember { mutableStateOf<String?>(null) }
    val activity = LocalActivity.current
    val context = LocalContext.current

    LaunchedEffect(activity?.intent?.dataString) {
        val currentIntent = activity?.intent ?: return@LaunchedEffect
        viewModel.handleEmailLoginFromIntent(
            intent = currentIntent,
            onSuccess = onContinue,
            onError = { error ->
                backendError.value = extractSimpleBackendError(error)
            }
        )
    }


    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = Dimens.screenPadding)
            .verticalScroll(scrollState)

    ) {
        // 🔹 Language Selector (Top-right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔙 Back arrow (left-aligned)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable { onBackClicked() }
                    .padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 🌐 Language selector (right-aligned)
            LanguageSelector(
                currentLanguage = currentLang,
                onClick = {
                    navController.navigate(Screen.Language.routeWithOrigin(Screen.Origin.LOGIN))
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // 🔹 Title
        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_phone_to_associated_to_account),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 🔹 Phone Field
        PhoneNumberInput(
            selectedCountry = selectedCountry,
            phoneNumber = phoneNumber,
            onPhoneNumberChange = viewModel::onPhoneNumberChanged,
            onFlagClick = { showCountryPicker = true }
        )

        Spacer(Modifier.height(16.dp))

        backendError.value?.let { message ->
            BackendErrorModal(
                message = message,
                onDismiss = { backendError.value = null }
            )
        }

        PrimaryButton(
            onClick = {
                val activity = activity
                if (activity != null) {
                    val phone = viewModel.phoneNumber
                    if (phone.isNotBlank()) {
                        reauthOtpViewModel.startReauthFlow(activity)
                        navController.navigate(Screen.Reauthenticate.baseRoute)
                    } else {
                        reauthOtpViewModel.errorHandled()
                    }
                } else {
                    reauthOtpViewModel.errorHandled()
                }
            },
            text = stringResource(R.string.continue_text),
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.isNotBlank(),
            isLoading = isAuthLoading,
            loadingText = "Please wait..."
        )

        Spacer(Modifier.height(12.dp))

        // 🔹 Forgot Password
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.trouble_loggin_in),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.recover_your_account),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .clickable(onClick = onForgotPassword)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )

            Spacer(modifier =  Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.or),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier =  Modifier.width(8.dp))

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        EmailSignInBtn(
            email = viewModel.email.value,
            onLinkSent = {
                viewModel.sendMagicLink(
                    context = context,
                    email = viewModel.email.value,
                    onSuccess = {
                        navController.navigate(Screen.EmailSent.routeWithEmail(viewModel.email.value))
                    },
                    onError = { e ->
                        backendError.value = extractSimpleBackendError(e)
                    }
                )
            },
            onError = { /* optional: fallback error handler */ },
            enabled = !isAuthLoading,
            isLoading = isAuthLoading
        )

        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        GoogleSignInBtn(
            launcher = googleLauncher,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAuthLoading,
            isLoading = isAuthLoading,
            onCredentialReceived = {
                viewModel.handleGoogleSignIn(
                    it,
                    GoogleAuthIntent.SIGN_IN,
                    {
                    onContinue()
                }) { e ->
                    backendError.value = extractSimpleBackendError(e)
                }
            },
            onError = {
                backendError.value = extractSimpleBackendError(it)
            }
        )


        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        FacebookSignInButton(
            activity = activity ?: return,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAuthLoading,
            isLoading = isAuthLoading,
            onClick = {
                viewModel.handleFacebookLogin(
                    activity = activity,
                    onSuccess = onContinue,
                    onError = { backendError.value = extractSimpleBackendError(it) }
                )
            }
        )
        Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

        // Dont have an account?
        AccountSwitchPrompt(
            variant = AccountSwitchVariant.DONT_HAVE_ACCOUNT,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            onActionClick = onSignUp,
        )


        // 🔹 Country Picker Dialog
        if (showCountryPicker) {
            CountryPickerBottomSheet(
                onDismiss = { showCountryPicker = false },
                onCountrySelected = {
                    viewModel.onCountrySelected(it)
                    showCountryPicker = false
                }
            )
        }
    }
}

package net.metalbrain.paysmart.core.features.account.creation.phone.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthIntent
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.ui.components.EmailSignInBtn
import net.metalbrain.paysmart.ui.components.FacebookSignInButton
import net.metalbrain.paysmart.core.features.account.components.GoogleSignInBtn
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.screens.loader.AppLoadingScreen
import net.metalbrain.paysmart.ui.screens.loader.rememberStabilizedLoading
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReauthOtpScreen(
    viewModel: ReauthOtpViewModel,
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showLoading = rememberStabilizedLoading(uiState.isLoading)
    val code by viewModel.code
    val context = LocalContext.current
    val color = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Google credential retrieval is handled by the provider callback.
    }
    var autoStartedOtp by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.factorsResolved, uiState.hasPhoneFactor) {
        if (uiState.factorsResolved && uiState.hasPhoneFactor && !autoStartedOtp) {
            Log.d("ReauthOtpScreen", "Starting OTP reauth flow")
            autoStartedOtp = true
            viewModel.startReauthFlow(activity)
        }
    }

    LaunchedEffect(activity.intent?.dataString) {
        viewModel.handleEmailReauthIntent(activity.intent, onSuccess)
    }

    if (!uiState.factorsResolved || (showLoading && uiState.hasPhoneFactor && code.isBlank())) {
        AppLoadingScreen(message = stringResource(R.string.loading_signing_in))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reauth_title)) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.hasPhoneFactor) {
                Text(
                    stringResource(R.string.reauth_verify_identity_message),
                    style = typography.bodyMedium,
                    color = color.textPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { viewModel.onCodeChange(it) },
                    label = { Text(stringResource(R.string.reauth_enter_otp_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = stringResource(R.string.reauth_verify_action),
                    isLoading = uiState.isLoading,
                    loadingText = stringResource(R.string.reauth_verifying),
                    onClick = { viewModel.reauthWithCode(onSuccess) },
                    enabled = code.length == 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.resendAvailable) {
                    TextButton(onClick = { viewModel.resendOtp(activity) }) {
                        Text(stringResource(R.string.reauth_resend_code))
                    }
                } else {
                    Text(
                        stringResource(
                            R.string.reauth_resend_available_in,
                            uiState.timerSeconds
                        ),
                        style = typography.bodyMedium,
                        color = color.textPrimary

                    )
                }
            } else {
                Text(stringResource(
                    R.string.reauth_verify_identity_message),
                    style = typography.bodyMedium,
                    color = color.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.reauth_alternative_factor_message),
                    style = typography.bodyMedium,
                    color = color.textPrimary
                )

                uiState.recoveryEmail?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.reauth_recovery_email_hint, it),
                        style = typography.bodySmall,
                        color = color.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.canUseEmailLink) {
                    EmailSignInBtn(
                        email = uiState.recoveryEmail.orEmpty(),
                        onLinkSent = { viewModel.sendEmailLink(context) },
                        onError = { error ->
                            viewModel.reportError(error.localizedMessage ?: "Email reauthentication failed")
                            Log.e("ReauthOtpScreen", "Email reauth setup failed", error)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        loadingText = stringResource(R.string.common_processing)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.canUseGoogle) {
                    GoogleSignInBtn(
                        clientId = stringResource(R.string.default_web_client_id),
                        launcher = googleLauncher,
                        intent = GoogleAuthIntent.SIGN_IN,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        loadingText = stringResource(R.string.common_processing),
                        onCredentialReceived = { credential ->
                            viewModel.reauthenticateWithGoogle(credential, onSuccess)
                        },
                        onError = {
                            viewModel.reportError(it.localizedMessage ?: "Google reauthentication failed")
                            Log.e("ReauthOtpScreen", "Google reauth failed", it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.canUseFacebook) {
                    FacebookSignInButton(
                        activity = activity,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        loadingText = stringResource(R.string.common_processing),
                        onClick = {
                            viewModel.reauthenticateWithFacebook(activity, onSuccess)
                        }
                    )
                }

                if (!uiState.canUseEmailLink && !uiState.canUseGoogle && !uiState.canUseFacebook) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.reauth_no_alternative_factor_message),
                        color = color.textPrimary
                    )
                }
            }

            if (uiState.infoMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.infoMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

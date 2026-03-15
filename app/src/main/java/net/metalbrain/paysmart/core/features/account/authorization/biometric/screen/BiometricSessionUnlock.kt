package net.metalbrain.paysmart.core.features.account.authorization.biometric.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarImage
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun BiometricSessionUnlock(
    onUnlock: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: BiometricOptInViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val activity = LocalActivity.current as? FragmentActivity ?: return
    val userState by userViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkBiometricSupport(activity)
    }

    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val profile = (userState as? UserUiState.ProfileLoaded)?.user
    val resolvedName = profile?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_default_name)
    val photoModel = profile?.photoURL

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = Dimens.screenPadding)
            .padding(bottom = Dimens.largeScreenPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatarImage(
                displayName = resolvedName,
                photoModel = photoModel,
                size = 164.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.welcome_back) + ", " + resolvedName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSpacing)
        ) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            PrimaryButton(
                enabled = true,
                text = stringResource(R.string.biometric_session_unlock_action),
                isLoading = isLoading,
                loadingText = stringResource(R.string.biometric_session_authenticating),
                onClick = {
                    viewModel.authenticateBiometric(
                        activity = activity,
                        onSuccess = onUnlock
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(onClick = onLogout) {
                Text(
                    text = stringResource(R.string.profile_logout),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

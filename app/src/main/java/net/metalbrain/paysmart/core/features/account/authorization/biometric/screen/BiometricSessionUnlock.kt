package net.metalbrain.paysmart.core.features.account.authorization.biometric.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.authorization.biometric.utils.SessionLogout
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileAvatarImage
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
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
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = securityStyle.outerHorizontalPadding)
            .padding(bottom = Dimens.largeScreenPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = if (editorialLayout) Alignment.Start else Alignment.CenterHorizontally
        ) {
            if (editorialLayout) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceElevated.copy(
                            alpha = securityStyle.glassPanelAlpha
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.xl),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = colors.fillHover.copy(alpha = 0.94f),
                                    shape = CircleShape
                                )
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = colors.brandPrimary
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                        ) {
                            Text(
                                text = resolvedName,
                                style = typography.heading2,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.welcome_back),
                                style = typography.bodyMedium,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                ProfileAvatarImage(
                    displayName = resolvedName,
                    photoModel = photoModel,
                    size = 164.dp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.welcome_back) + ", " + resolvedName,
                    style = typography.heading3,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = if (editorialLayout) Alignment.Start else Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSpacing)
        ) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = colors.error,
                    style = typography.bodySmall,
                    textAlign = if (editorialLayout) TextAlign.Start else TextAlign.Center
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

            SessionLogout(
                onClick = onLogout,
                alignment = if (editorialLayout) Alignment.Start else Alignment.CenterHorizontally
            )

        }
    }
}

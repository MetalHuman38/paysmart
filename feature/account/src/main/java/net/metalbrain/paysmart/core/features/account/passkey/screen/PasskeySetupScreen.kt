package net.metalbrain.paysmart.core.features.account.passkey.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.core.features.account.authorization.passcode.card.BrandFooter
import net.metalbrain.paysmart.core.features.account.passkey.cards.PasskeySurfaceCard
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyBackButton
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeySecurityIcon
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyScreenBackground
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeySecurityPanel
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyStatusTone
import net.metalbrain.paysmart.core.features.account.passkey.utils.passkeyContentPadding
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SecondaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun PasskeySetupScreen(
    activity: Activity,
    viewModel: PasskeySetupViewModel,
    onRegistered: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            onRegistered()
        }
    }

    PasskeyScreenBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(passkeyContentPadding())
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                PasskeyBackButton(onBack = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Text(
                        text = stringResource(R.string.passkey_setup_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.passkey_setup_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PasskeySurfaceCard(
                    accentColor = MaterialTheme.colorScheme.primary,
                    highlighted = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.lg),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PasskeySecurityIcon()

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_passkey_settings_toggle),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(
                                        if (state.isRegistered) {
                                            R.string.profile_security_passkey_subtitle_enabled
                                        } else {
                                            R.string.profile_security_passkey_subtitle_disabled
                                        }
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        PrimaryButton(
                            text = stringResource(R.string.passkey_create_action),
                            onClick = { viewModel.registerPasskey(activity) },
                            enabled = !state.isLoading && !state.isRegistered,
                            isLoading = state.isLoading,
                            loadingText = stringResource(R.string.common_processing)
                        )

                        SecondaryButton(
                            text = stringResource(R.string.passkey_verify_action),
                            onClick = { viewModel.verifyPasskey(activity) },
                            enabled = !state.isLoading
                        )

                        if (state.isLoading) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(R.raw.loader)
                            )
                            val progress by animateLottieCompositionAsState(
                                composition,
                                iterations = LottieConstants.IterateForever,
                                isPlaying = true
                            )
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                            )
                        }
                    }
                }

                PasskeySecurityPanel(
                    title = when {
                        state.error != null -> state.error.orEmpty()
                        state.isRegistered -> stringResource(R.string.passkey_registered)
                        else -> stringResource(R.string.profile_passkey_settings_status_disabled)
                    },
                    subtitle = when {
                        state.error != null -> stringResource(R.string.profile_security_passkey_subtitle_disabled)
                        state.isRegistered -> stringResource(R.string.profile_security_passkey_subtitle_enabled)
                        else -> stringResource(R.string.passkey_setup_description)
                    },
                    supporting = null,
                    tone = when {
                        state.error != null -> PasskeyStatusTone.Danger
                        state.isRegistered -> PasskeyStatusTone.Active
                        else -> PasskeyStatusTone.Neutral
                    }
                )
            }

            BrandFooter()
        }
    }
}

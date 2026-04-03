package net.metalbrain.paysmart.core.features.account.passkey.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.core.features.account.passkey.cards.PasskeySurfaceCard
import net.metalbrain.paysmart.core.features.account.passkey.components.CredentialRow
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyBackButton
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyGlowSwitch
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyScreenBackground
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeySecurityIcon
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeySecurityPanel
import net.metalbrain.paysmart.core.features.account.passkey.components.PasskeyStatusTone
import net.metalbrain.paysmart.core.features.account.passkey.utils.passkeyContentPadding
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun ProfilePasskeySettingsScreen(
    activity: Activity,
    viewModel: PasskeySetupViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LocalAppThemePack.current.securityStyle
    val enabled = state.isRegistered

    LaunchedEffect(Unit) {
        viewModel.refreshCredentialList()
    }

    PasskeyScreenBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = passkeyContentPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            item {
                PasskeyBackButton(onBack = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Text(
                        text = stringResource(R.string.profile_passkey_settings_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.profile_passkey_settings_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                PasskeySurfaceCard(
                    accentColor = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    highlighted = enabled
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = Dimens.xs),
                            contentAlignment = Alignment.Center
                        ) {
                            PasskeySecurityIcon()
                        }

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
                                    if (enabled) {
                                        R.string.profile_security_passkey_subtitle_enabled
                                    } else {
                                        R.string.profile_security_passkey_subtitle_disabled
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        PasskeyGlowSwitch(
                            checked = enabled,
                            enabled = !state.isLoading && state.activeRevokeCredentialId == null,
                            onCheckedChange = { shouldEnable ->
                                if (shouldEnable) {
                                    viewModel.registerPasskey(activity)
                                } else {
                                    viewModel.disablePasskey()
                                }
                            }
                        )
                    }
                }
            }

            if (state.credentials.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.profile_passkey_settings_registered_devices),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(state.credentials, key = { it.credentialId }) { credential ->
                    CredentialRow(
                        credential = credential,
                        isRevoking = state.activeRevokeCredentialId == credential.credentialId,
                        onRevoke = { viewModel.revokeCredential(credential.credentialId) }
                    )
                }
            }

            item {
                val tone = when {
                    state.error != null -> PasskeyStatusTone.Danger
                    enabled -> PasskeyStatusTone.Active
                    else -> PasskeyStatusTone.Neutral
                }
                val title = when {
                    state.error != null -> state.error.orEmpty()
                    enabled -> stringResource(R.string.passkey_registered)
                    else -> stringResource(R.string.profile_passkey_settings_status_disabled)
                }
                val subtitle = when {
                    state.error != null -> stringResource(R.string.profile_security_passkey_subtitle_disabled)
                    enabled -> stringResource(R.string.profile_security_passkey_subtitle_enabled)
                    else -> stringResource(R.string.profile_security_passkey_subtitle_disabled)
                }
                PasskeySecurityPanel(
                    title = title,
                    subtitle = subtitle,
                    supporting = state.statusMessage,
                    tone = tone
                )
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileSecurityActionRow
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileSecurityToggleRow
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSecurityPrivacyScreen(
    settings: LocalSecuritySettingsModel?,
    hideBalanceEnabled: Boolean,
    onBack: () -> Unit,
    onResetPassword: () -> Unit,
    onTransactionPin: () -> Unit,
    onPasskeySettings: () -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onViewPrivacySettings: () -> Unit,
    onHideBalanceToggle: (Boolean) -> Unit
) {
    val isPasswordReady = settings?.passwordEnabled == true && settings.localPasswordSetAt != null
    val isPasscodeReady = settings?.passcodeEnabled == true && settings.localPassCodeSetAt != null
    val isPasskeyEnabled = settings?.passkeyEnabled == true
    val isBiometricEnabled = settings?.biometricsEnabled == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_menu_security_privacy_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileSecurityActionRow(
                        title = stringResource(R.string.profile_security_reset_password),
                        icon = Icons.Default.Lock,
                        subtitle = if (isPasswordReady) {
                            stringResource(R.string.common_completed)
                        } else {
                            stringResource(R.string.common_required)
                        },
                        onClick = onResetPassword
                    )
                    HorizontalDivider()
                    ProfileSecurityActionRow(
                        title = stringResource(R.string.profile_security_transaction_pin),
                        icon = Icons.Default.Security,
                        subtitle = if (isPasscodeReady) {
                            stringResource(R.string.common_completed)
                        } else {
                            stringResource(R.string.common_required)
                        },
                        onClick = onTransactionPin
                    )
                    HorizontalDivider()
                    ProfileSecurityActionRow(
                        title = stringResource(R.string.profile_security_passkey_title),
                        icon = Icons.Default.Lock,
                        subtitle = if (isPasskeyEnabled) {
                            stringResource(R.string.profile_security_passkey_subtitle_enabled)
                        } else {
                            stringResource(R.string.profile_security_passkey_subtitle_disabled)
                        },
                        onClick = onPasskeySettings
                    )
                    HorizontalDivider()
                    ProfileSecurityToggleRow(
                        title = stringResource(R.string.profile_security_biometric_toggle),
                        icon = Icons.Default.Security,
                        checked = isBiometricEnabled,
                        onCheckedChange = onBiometricToggle
                    )
                    HorizontalDivider()
                    ProfileSecurityActionRow(
                        title = stringResource(R.string.profile_security_view_privacy_settings),
                        icon = Icons.Default.Security,
                        onClick = onViewPrivacySettings
                    )
                    HorizontalDivider()
                    ProfileSecurityToggleRow(
                        title = stringResource(R.string.profile_security_hide_balance),
                        icon = Icons.Default.VisibilityOff,
                        checked = hideBalanceEnabled,
                        onCheckedChange = onHideBalanceToggle
                    )
                }
            }
        }
    }
}

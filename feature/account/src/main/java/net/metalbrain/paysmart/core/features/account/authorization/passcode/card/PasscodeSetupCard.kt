package net.metalbrain.paysmart.core.features.account.authorization.passcode.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.PasscodeField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack


@Composable
fun PasscodeSetupCard(
    passcode: String,
    confirm: String,
    showPasscode: Boolean,
    showConfirmPasscode: Boolean,
    isMismatch: Boolean,
    error: String?,
    isLoading: Boolean,
    canSubmit: Boolean,
    onPasscodeChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onTogglePasscode: () -> Unit,
    onToggleConfirmPasscode: () -> Unit,
    onSubmit: () -> Unit,
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (securityStyle.useEditorialLayout) {
                MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = securityStyle.glassPanelAlpha)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(if (securityStyle.useEditorialLayout) Dimens.lg else Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                Text(
                    text = stringResource(R.string.set_passcode_save_action),
                    style = if (securityStyle.useEditorialLayout) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.add_passcode_advisory_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PasscodeField(
                value = passcode,
                onValueChange = onPasscodeChange,
                label = stringResource(R.string.set_passcode_enter_label),
                showText = showPasscode,
                onToggleVisibility = onTogglePasscode
            )

            PasscodeField(
                value = confirm,
                onValueChange = onConfirmChange,
                label = stringResource(R.string.set_passcode_confirm_label),
                showText = showConfirmPasscode,
                onToggleVisibility = onToggleConfirmPasscode,
                isError = isMismatch
            )

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = stringResource(R.string.set_passcode_save_action),
                onClick = onSubmit,
                enabled = canSubmit,
                isLoading = isLoading,
                loadingText = stringResource(R.string.set_passcode_saving),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

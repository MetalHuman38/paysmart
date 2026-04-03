package net.metalbrain.paysmart.core.features.account.authorization.passcode.card

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.PasscodeField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ChangePasscodeCard(
    currentPasscode: String,
    newPasscode: String,
    confirmPasscode: String,
    showCurrentPasscode: Boolean,
    showNewPasscode: Boolean,
    showConfirmPasscode: Boolean,
    isMismatch: Boolean,
    error: String?,
    isLoading: Boolean,
    canSubmit: Boolean,
    onCurrentPasscodeChange: (String) -> Unit,
    onNewPasscodeChange: (String) -> Unit,
    onConfirmPasscodeChange: (String) -> Unit,
    onToggleCurrentPasscode: () -> Unit,
    onToggleNewPasscode: () -> Unit,
    onToggleConfirmPasscode: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                Text(
                    text = stringResource(R.string.change_passcode_update_action),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.change_passcode_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PasscodeField(
                value = currentPasscode,
                onValueChange = onCurrentPasscodeChange,
                label = stringResource(R.string.change_passcode_current_label),
                showText = showCurrentPasscode,
                onToggleVisibility = onToggleCurrentPasscode
            )

            PasscodeField(
                value = newPasscode,
                onValueChange = onNewPasscodeChange,
                label = stringResource(R.string.change_passcode_new_label),
                showText = showNewPasscode,
                onToggleVisibility = onToggleNewPasscode
            )

            PasscodeField(
                value = confirmPasscode,
                onValueChange = onConfirmPasscodeChange,
                label = stringResource(R.string.change_passcode_confirm_label),
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
                text = stringResource(R.string.change_passcode_update_action),
                onClick = onSubmit,
                enabled = canSubmit,
                isLoading = isLoading,
                loadingText = stringResource(R.string.change_passcode_updating),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

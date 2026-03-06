package net.metalbrain.paysmart.core.features.account.passkey.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyCredentialItem
import java.text.DateFormat
import java.util.Date

@Composable
fun CredentialRow(
    credential: PasskeyCredentialItem,
    isRevoking: Boolean,
    onRevoke: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val label = buildCredentialLabel(credential)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.profile_passkey_credential_added_at,
                    formatTimestamp(credential.createdAtMs)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.profile_passkey_credential_id_short,
                    "${credential.credentialId.take(10)}..."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onRevoke,
                enabled = !isRevoking
            ) {
                Text(
                    if (isRevoking) {
                        stringResource(R.string.profile_passkey_credential_action_revoking)
                    } else {
                        stringResource(R.string.profile_passkey_credential_action_revoke)
                    }
                )
            }
        }
    }
}

@Composable
private fun buildCredentialLabel(credential: PasskeyCredentialItem): String {
    val deviceType = credential.deviceType?.replaceFirstChar { it.uppercaseChar() }
        ?: stringResource(R.string.profile_passkey_credential_device_fallback)
    val backup = if (credential.backedUp) {
        stringResource(R.string.profile_passkey_credential_backup_backed_up)
    } else {
        stringResource(R.string.profile_passkey_credential_backup_local_only)
    }
    return stringResource(
        R.string.profile_passkey_credential_label_format,
        deviceType,
        backup
    )
}

@Composable
private fun formatTimestamp(timestampMs: Long): String {
    if (timestampMs <= 0L) return stringResource(R.string.profile_passkey_credential_unknown_time)
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return formatter.format(Date(timestampMs))
}

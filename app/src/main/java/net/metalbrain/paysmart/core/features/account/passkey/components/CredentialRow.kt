package net.metalbrain.paysmart.core.features.account.passkey.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyCredentialItem
import net.metalbrain.paysmart.ui.theme.Dimens
import java.text.DateFormat
import java.util.Date
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.account.passkey.cards.PasskeySurfaceCard

@Composable
fun CredentialRow(
    credential: PasskeyCredentialItem,
    isRevoking: Boolean,
    onRevoke: () -> Unit
) {
    val presentation = rememberCredentialPresentation(credential)
    PasskeySurfaceCard(
        accentColor = MaterialTheme.colorScheme.primary,
        highlighted = credential.backedUp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = presentation.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = presentation.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_passkey_settings_status_enabled),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
                        "${credential.credentialId.take(12)}..."
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onRevoke,
                    enabled = !isRevoking
                ) {
                    Text(
                        text = if (isRevoking) {
                            stringResource(R.string.profile_passkey_credential_action_revoking)
                        } else {
                            stringResource(R.string.profile_passkey_credential_action_revoke)
                        },
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCredentialPresentation(
    credential: PasskeyCredentialItem
): CredentialPresentation {
    val deviceType = credential.deviceType.orEmpty().lowercase()
    val icon = when {
        deviceType.contains("phone") || deviceType.contains("android") || deviceType.contains("ios") -> {
            Icons.Outlined.PhoneAndroid
        }

        deviceType.contains("laptop") || deviceType.contains("desktop") || deviceType.contains("mac") || deviceType.contains("windows") -> {
            Icons.Outlined.LaptopMac
        }

        else -> Icons.Outlined.Devices
    }

    val title = credential.deviceType
        ?.takeIf { it.isNotBlank() }
        ?.replaceFirstChar { it.uppercaseChar() }
        ?: stringResource(R.string.profile_passkey_credential_device_fallback)

    val subtitle = if (credential.backedUp) {
        stringResource(R.string.profile_passkey_credential_backup_backed_up)
    } else {
        stringResource(R.string.profile_passkey_credential_backup_local_only)
    }
    return CredentialPresentation(icon = icon, title = title, subtitle = subtitle)
}

private data class CredentialPresentation(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

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

package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLimitsScreen(
    settings: LocalSecuritySettingsModel?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_limits_title)) },
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
                    LimitsRow(
                        label = stringResource(R.string.account_limits_current_tier),
                        value = resolveTierLabel(settings)
                    )
                    HorizontalDivider()
                    LimitsRow(
                        label = stringResource(R.string.account_limits_identity_status),
                        value = if (settings?.hasVerifiedIdentity == true) {
                            stringResource(R.string.common_verified)
                        } else {
                            stringResource(R.string.common_not_verified)
                        }
                    )
                    HorizontalDivider()
                    LimitsRow(
                        label = stringResource(R.string.account_limits_address_status),
                        value = if (settings?.hasAddedHomeAddress == true) {
                            stringResource(R.string.common_completed)
                        } else {
                            stringResource(R.string.common_required)
                        }
                    )
                    HorizontalDivider()
                    LimitsRow(
                        label = stringResource(R.string.account_limits_email_status),
                        value = if (settings?.hasVerifiedEmail == true) {
                            stringResource(R.string.common_verified)
                        } else {
                            stringResource(R.string.common_not_verified)
                        }
                    )
                }
            }

            Text(
                text = stringResource(
                    R.string.account_limits_last_synced,
                    formatLastSynced(
                        lastSynced = settings?.lastSynced ?: 0L,
                        neverValue = stringResource(R.string.common_never)
                    )
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.account_limits_info_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun resolveTierLabel(settings: LocalSecuritySettingsModel?): String {
    return when {
        settings?.hasVerifiedIdentity == true && settings.hasAddedHomeAddress == true ->
            stringResource(R.string.account_limits_tier_enhanced)

        settings?.hasVerifiedEmail == true ->
            stringResource(R.string.account_limits_tier_standard)

        else ->
            stringResource(R.string.account_limits_tier_restricted)
    }
}

@Composable
private fun LimitsRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatLastSynced(
    lastSynced: Long,
    neverValue: String
): String {
    if (lastSynced <= 0L) {
        return neverValue
    }
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(lastSynced))
}

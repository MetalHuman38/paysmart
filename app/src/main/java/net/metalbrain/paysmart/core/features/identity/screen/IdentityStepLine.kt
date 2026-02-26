package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R

enum class IdentityStepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

@Composable
fun IdentityStepLine(
    label: String,
    status: IdentityStepStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = when (status) {
                IdentityStepStatus.PENDING -> stringResource(R.string.identity_resolver_status_pending)
                IdentityStepStatus.IN_PROGRESS -> stringResource(R.string.identity_resolver_status_in_progress)
                IdentityStepStatus.COMPLETED -> stringResource(R.string.identity_resolver_status_completed)
                IdentityStepStatus.FAILED -> stringResource(R.string.identity_resolver_status_failed)
            },
            style = MaterialTheme.typography.labelMedium,
            color = when (status) {
                IdentityStepStatus.FAILED -> MaterialTheme.colorScheme.error
                IdentityStepStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

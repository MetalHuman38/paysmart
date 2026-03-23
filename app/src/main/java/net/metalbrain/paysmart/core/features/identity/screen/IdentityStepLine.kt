package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

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
    val statusColor = when (status) {
        IdentityStepStatus.FAILED -> MaterialTheme.colorScheme.error
        IdentityStepStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        IdentityStepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        IdentityStepStatus.PENDING -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.sm)
                    .background(
                        color = statusColor.copy(
                            alpha = if (status == IdentityStepStatus.PENDING) 0.56f else 1f
                        ),
                        shape = CircleShape
                    )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            color = badgeContainerColor(statusColor, status),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = when (status) {
                    IdentityStepStatus.PENDING -> stringResource(R.string.identity_resolver_status_pending)
                    IdentityStepStatus.IN_PROGRESS -> stringResource(R.string.identity_resolver_status_in_progress)
                    IdentityStepStatus.COMPLETED -> stringResource(R.string.identity_resolver_status_completed)
                    IdentityStepStatus.FAILED -> stringResource(R.string.identity_resolver_status_failed)
                },
                modifier = Modifier.padding(horizontal = Dimens.sm, vertical = Dimens.xs),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun badgeContainerColor(
    statusColor: Color,
    status: IdentityStepStatus
): Color {
    return when (status) {
        IdentityStepStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        else -> statusColor.copy(alpha = 0.14f)
    }
}

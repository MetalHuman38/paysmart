package net.metalbrain.paysmart.core.features.account.passkey.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun PasskeySecurityPanel(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tone: PasskeyStatusTone = PasskeyStatusTone.Neutral,
    supporting: String? = null
) {
    val icon = when (tone) {
        PasskeyStatusTone.Active -> Icons.Outlined.CheckCircle
        PasskeyStatusTone.Danger -> Icons.Outlined.ErrorOutline
        PasskeyStatusTone.Neutral -> Icons.Outlined.VerifiedUser
    }

    val accentColor = when (tone) {
        PasskeyStatusTone.Active -> MaterialTheme.colorScheme.primary
        PasskeyStatusTone.Danger -> MaterialTheme.colorScheme.error
        PasskeyStatusTone.Neutral -> MaterialTheme.colorScheme.secondary
    }

    val containerColor = when (tone) {
        PasskeyStatusTone.Active -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        PasskeyStatusTone.Danger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        PasskeyStatusTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.20f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                supporting?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }
        }
    }
}

package net.metalbrain.paysmart.core.features.addmoney.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.core.features.addmoney.util.AddMoneySessionVisualState
import net.metalbrain.paysmart.core.features.addmoney.util.resolveAddMoneySessionVisualState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun AddMoneySessionStatusCard(
    sessionId: String?,
    sessionStatus: AddMoneySessionStatus?,
    provider: AddMoneyProvider?,
    modifier: Modifier = Modifier
) {
    if (sessionId == null && sessionStatus == null && provider == null) return

    val visualState = resolveAddMoneySessionVisualState(sessionStatus, provider)
    val tone = resolveSessionTone(visualState)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = tone.containerColor,
            contentColor = tone.contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.minimumTouchTarget),
                    shape = MaterialTheme.shapes.medium,
                    color = tone.iconContainerColor
                ) {
                    Icon(
                        imageVector = tone.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(Dimens.sm)
                            .size(Dimens.lg),
                        tint = tone.iconTint
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = visualState.title(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    visualState.supportingText()?.let { supporting ->
                        Text(
                            text = supporting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tone.contentColor.copy(alpha = 0.88f)
                        )
                    }
                }
            }

            provider?.let { value ->
                SessionMetaLine(
                    label = stringResource(R.string.add_money_session_provider_label),
                    value = providerLabel(value)
                )
            }

            sessionStatus?.let { value ->
                SessionMetaLine(
                    label = stringResource(R.string.add_money_session_status_label),
                    value = value.displayLabel()
                )
            }

            sessionId?.let { value ->
                SessionMetaLine(
                    label = stringResource(R.string.add_money_session_id_label),
                    value = value
                )
            }
        }
    }
}

@Composable
private fun SessionMetaLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AddMoneySessionVisualState?.title(): String {
    return when (this) {
        AddMoneySessionVisualState.FLUTTERWAVE_READY ->
            stringResource(R.string.add_money_session_flutterwave_ready_title)
        AddMoneySessionVisualState.SESSION_READY ->
            stringResource(R.string.add_money_session_ready_title)
        AddMoneySessionVisualState.PENDING ->
            stringResource(R.string.add_money_session_pending_title)
        AddMoneySessionVisualState.SUCCEEDED ->
            stringResource(R.string.add_money_session_succeeded_title)
        AddMoneySessionVisualState.FAILED ->
            stringResource(R.string.add_money_session_failed_title)
        AddMoneySessionVisualState.EXPIRED ->
            stringResource(R.string.add_money_session_expired_title)
        null -> stringResource(R.string.add_money_session_ready_title)
    }
}

@Composable
private fun AddMoneySessionVisualState?.supportingText(): String? {
    return when (this) {
        AddMoneySessionVisualState.FLUTTERWAVE_READY ->
            stringResource(R.string.add_money_session_flutterwave_ready_supporting)
        AddMoneySessionVisualState.SESSION_READY ->
            stringResource(R.string.add_money_session_ready_supporting)
        AddMoneySessionVisualState.PENDING ->
            stringResource(R.string.add_money_session_pending_supporting)
        AddMoneySessionVisualState.SUCCEEDED ->
            stringResource(R.string.add_money_session_succeeded_supporting)
        AddMoneySessionVisualState.FAILED ->
            stringResource(R.string.add_money_session_failed_supporting)
        AddMoneySessionVisualState.EXPIRED ->
            stringResource(R.string.add_money_session_expired_supporting)
        null -> null
    }
}

@Composable
private fun resolveSessionTone(visualState: AddMoneySessionVisualState?): SessionTone {
    val colorScheme = MaterialTheme.colorScheme
    return when (visualState) {
        AddMoneySessionVisualState.FLUTTERWAVE_READY,
        AddMoneySessionVisualState.SUCCEEDED -> SessionTone(
            icon = Icons.Filled.CheckCircle,
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            iconContainerColor = colorScheme.onTertiaryContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onTertiaryContainer
        )

        AddMoneySessionVisualState.FAILED -> SessionTone(
            icon = Icons.Filled.ErrorOutline,
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            iconContainerColor = colorScheme.onErrorContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onErrorContainer
        )

        AddMoneySessionVisualState.EXPIRED -> SessionTone(
            icon = Icons.Filled.Schedule,
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            iconContainerColor = colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onSecondaryContainer
        )

        AddMoneySessionVisualState.SESSION_READY,
        AddMoneySessionVisualState.PENDING,
        null -> SessionTone(
            icon = Icons.Filled.Verified,
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
            iconContainerColor = colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            iconTint = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun providerLabel(provider: AddMoneyProvider): String {
    return when (provider) {
        AddMoneyProvider.STRIPE -> stringResource(R.string.add_money_provider_stripe)
        AddMoneyProvider.FLUTTERWAVE -> stringResource(R.string.add_money_provider_flutterwave)
    }
}

private fun AddMoneySessionStatus.displayLabel(): String {
    return when (this) {
        AddMoneySessionStatus.CREATED -> "Created"
        AddMoneySessionStatus.PENDING -> "Pending"
        AddMoneySessionStatus.SUCCEEDED -> "Succeeded"
        AddMoneySessionStatus.FAILED -> "Failed"
        AddMoneySessionStatus.EXPIRED -> "Expired"
    }
}

private data class SessionTone(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val iconContainerColor: Color,
    val iconTint: Color
)

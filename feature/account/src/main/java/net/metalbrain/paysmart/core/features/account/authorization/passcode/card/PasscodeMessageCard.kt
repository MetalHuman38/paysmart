package net.metalbrain.paysmart.core.features.account.authorization.passcode.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PasscodeMessageCard(
    message: String,
    isError: Boolean
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (securityStyle.useEditorialLayout) 22.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = if (securityStyle.useEditorialLayout) 0.62f else 0.8f)
            } else {
                if (securityStyle.useEditorialLayout) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = securityStyle.glassPanelAlpha)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

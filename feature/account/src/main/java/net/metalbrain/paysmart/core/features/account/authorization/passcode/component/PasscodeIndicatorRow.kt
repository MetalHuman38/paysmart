package net.metalbrain.paysmart.core.features.account.authorization.passcode.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PasscodeIndicatorRow(
    passcode: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm, Alignment.CenterHorizontally)
    ) {
        repeat(6) { index ->
            val filled = index < passcode.length
            val containerColor = when {
                isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.78f)
                filled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                securityStyle.useEditorialLayout -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .size(width = 42.dp, height = 54.dp)
                    .clip(RoundedCornerShape(if (securityStyle.useEditorialLayout) 18.dp else 16.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = when {
                                    isError -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.utils.PasswordChecks

@Composable
fun StrengthMeter(checks: PasswordChecks) {
    val strength = checks.strength
    val color = when (checks.score) {
        0, 1 -> MaterialTheme.colorScheme.error
        2 -> MaterialTheme.colorScheme.tertiary
        3, 4 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = checks.label(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
